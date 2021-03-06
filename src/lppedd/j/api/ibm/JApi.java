/*
 * The MIT License
 *
 * Copyright (c) 2017 Edoardo Luppi <lp.edoardo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lppedd.j.api.ibm;

import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

import lppedd.j.api.JBase;
import lppedd.j.api.JConnection;
import lppedd.j.api.files.JAbstractFile;
import lppedd.j.api.files.JFile;
import lppedd.j.api.files.device.JSaveFile;
import lppedd.j.api.members.JMember;
import lppedd.j.api.misc.EmptyArrays;
import lppedd.j.api.misc.JUtil;
import lppedd.j.api.objects.JObject;
import lppedd.j.api.objects.JUserSpace;
import lppedd.j.api.objects.JUserSpace.JUserSpaceBuilder;

/**
 * A collection of IBMi API wrappers.
 *
 * @author Edoardo Luppi
 */
public final class JApi implements IBMiDataTypes
{
   private static final AS400Message ERROR_MESSAGE = JUtil.newMessage("ERROR", "Program not called correctly");

   /**
    * Deletes a user space object.
    *
    * @see <a href="https://www.ibm.com/support/knowledgecenter/en/ssw_ibm_i_72/apis/qusdltus.htm">IBM Knowledge Center</a>
    *
    * @param userSpace
    *        The user space to delete
    */
   public static boolean QUSDLTUS(final JUserSpace userSpace) {
      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(userSpace.getConnection().getAs400(), "/QSYS.LIB/QUSDLTUS.PGM", parameters);

      try {
         if (pgmCall.run()) {
            return pgmCall.getMessageList().length == 0;
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }

   public static JApiResult QSRLSAVF(final JConnection connection, final String format, final String objectFilter, final String typeFilter, final JSaveFile object) {
      final JUserSpace userSpace = new JUserSpaceBuilder(connection, JUtil.getRandomString(10), "QTEMP")
            .length(80000)
            .autoExtendible(true)
            .initialValue((byte) 0x00)
            .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JApiResult(ERROR_MESSAGE);
      }

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
            new ProgramParameter(CHAR10.toBytes(objectFilter)),
            new ProgramParameter(CHAR10.toBytes(typeFilter)),
            new ProgramParameter(CHAR36.toBytes("")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QSRLSAVF.PGM", parameters);
      byte[] buffer = EmptyArrays.EMPTY_BYTE;

      try {
         if (pgmCall.run()) {
            final int userSpaceLength = userSpace.getLength();
            buffer = new byte[userSpaceLength];
            userSpace.read(buffer, 0, 0, userSpaceLength);
         }
      } catch (final AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      userSpace.delete();
      return new JApiResult(buffer, pgmCall.getMessageList());
   }

   public static JApiResult QUSLFLD(final JConnection connection, final String format, final String recordFormat, final JFile object) {
      final JUserSpace userSpace = new JUserSpaceBuilder(connection, JUtil.getRandomString(10), "QTEMP")
            .length(80000)
            .autoExtendible(true)
            .initialValue((byte) 0x00)
            .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JApiResult(ERROR_MESSAGE);
      }

      userSpace.setText("QUSLFLD userspace");
      userSpace.persist();

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
            new ProgramParameter(CHAR10.toBytes(recordFormat)),
            new ProgramParameter(CHAR1.toBytes("0")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QUSLFLD.PGM", parameters);
      byte[] buffer = EmptyArrays.EMPTY_BYTE;

      try {
         if (pgmCall.run()) {
            final int userSpaceLength = userSpace.getLength();
            buffer = new byte[userSpaceLength];
            userSpace.read(buffer, 0, 0, userSpaceLength);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      userSpace.delete();
      return new JApiResult(buffer, pgmCall.getMessageList());
   }

   public static JApiResult QUSLRCD(final JConnection connection, final String format, final JFile object) {
      final JUserSpace userSpace = new JUserSpaceBuilder(connection, JUtil.getRandomString(10), "QTEMP")
            .length(20000)
            .autoExtendible(true)
            .initialValue((byte) 0x00)
            .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JApiResult(ERROR_MESSAGE);
      }

      userSpace.setText("QUSLRCD userspace");
      userSpace.persist();

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
            new ProgramParameter(CHAR1.toBytes("0")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400(), "/QSYS.LIB/QUSLRCD.PGM", parameters);
      byte[] buffer = EmptyArrays.EMPTY_BYTE;

      try {
         if (pgmCall.run()) {
            final int userSpaceLength = userSpace.getLength();
            buffer = new byte[userSpaceLength];
            userSpace.read(buffer, 0, 0, userSpaceLength);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | InterruptedException | IOException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      userSpace.delete();
      return new JApiResult(buffer, pgmCall.getMessageList());
   }

   public static JApiResult QCLRPGMI(final JConnection connection, final String format, final JObject object) {
      int formatLenght = 0;

      switch (format) {
         case "PGMI0100":
            formatLenght = 537;
            break;
         default:
            break;
      }

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(formatLenght),
            new ProgramParameter(BIN4.toBytes(formatLenght)),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QCLRPGMI.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = BinaryConverter.byteArrayToInt(output, 0);
            final int availableBytes = BinaryConverter.byteArrayToInt(output, 4);

            if (availableBytes > returnedBytes) {
               parameters[0] = new ProgramParameter(availableBytes);
               parameters[1] = new ProgramParameter(BIN4.toBytes(availableBytes));
               pgmCall.setParameterList(parameters);
               continue;
            }
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException | PropertyVetoException e) {
         e.printStackTrace();
      }

      return new JApiResult(output, pgmCall.getMessageList());
   }

   public static JApiResult QUSROBJD(final JConnection connection, final String format, final JObject object) {
      int formatLenght = 0;

      switch (format) {
         case "OBJD0100":
            formatLenght = 90;
            break;
         case "OBJD0200":
            formatLenght = 180;
            break;
         case "OBJD0300":
            formatLenght = 460;
            break;
         case "OBJD0400":
            formatLenght = 667;
            break;
         default:
            break;
      }

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(formatLenght),
            new ProgramParameter(BIN4.toBytes(formatLenght)),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
            new ProgramParameter(CHAR10.toBytes("*" + object.getType())),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QUSROBJD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = BinaryConverter.byteArrayToInt(output, 0);
            final int availableBytes = BinaryConverter.byteArrayToInt(output, 4);

            if (availableBytes <= returnedBytes) {
               break;
            }

            parameters[0] = new ProgramParameter(availableBytes);
            parameters[1] = new ProgramParameter(BIN4.toBytes(availableBytes));
            pgmCall.setParameterList(parameters);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException | PropertyVetoException e) {
         e.printStackTrace();
      }

      return new JApiResult(output, pgmCall.getMessageList());
   }

   public static JApiResult QDBRTVFD(final JConnection connection, final String format, final JAbstractFile file) {
      byte[] recordFormatName = CHAR10.toBytes("");
      int formatLength = 0;

      switch (format) {
         case "FILD0100":
         case "FILD0300":
         case "FILD0400":
            formatLength = 2048;
            break;
         case "FILD0200":
            recordFormatName = CHAR10.toBytes("*FIRST");
            formatLength = 2048;
            break;
         default:
            break;
      }

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(formatLength),
            new ProgramParameter(BIN4.toBytes(formatLength)),
            new ProgramParameter(20),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(file.getQualifiedPath())),
            new ProgramParameter(recordFormatName),
            new ProgramParameter(CHAR1.toBytes("0")),
            new ProgramParameter(CHAR10.toBytes("*LCL")),
            new ProgramParameter(CHAR10.toBytes("*EXT")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QDBRTVFD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = BinaryConverter.byteArrayToInt(output, 0);
            final int availableBytes = BinaryConverter.byteArrayToInt(output, 4);

            if (availableBytes <= returnedBytes) {
               break;
            }

            parameters[0] = new ProgramParameter(availableBytes);
            parameters[1] = new ProgramParameter(BIN4.toBytes(availableBytes));
            pgmCall.setParameterList(parameters);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException | PropertyVetoException e) {
         e.printStackTrace();
      }

      return new JApiResult(output, pgmCall.getMessageList());
   }

   public static JApiResult QUSRMBRD(final JConnection connection, final String format, final JBase base) {
      int formatLenght = 0;

      switch (format) {
         case "MBRD0100":
            formatLenght = 135;
            break;
         case "MBRD0200":
            formatLenght = 550;
            break;
         case "MBRD0300":
            formatLenght = 780;
            break;
         case "MBRD0400":
            formatLenght = 102068;
            break;
         case "MBRD0500":
            formatLenght = 16;
            break;
         default:
            break;
      }

      final String object = base instanceof JMember ? ((JMember) base).getObject() : base.getName();
      final String member = base instanceof JMember ? base.getName() : "*FIRST";

      final ProgramParameter[] parameters = new ProgramParameter[] {
            new ProgramParameter(formatLenght),
            new ProgramParameter(BIN4.toBytes(formatLenght)),
            new ProgramParameter(CHAR8.toBytes(format)),
            new ProgramParameter(CHAR20.toBytes(JUtil.getQualifiedPath(base.getLibrary(), object))),
            new ProgramParameter(CHAR10.toBytes(member)),
            new ProgramParameter(CHAR1.toBytes("1")),
            new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(connection.getAs400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QUSRMBRD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = BinaryConverter.byteArrayToInt(output, 0);
            final int availableBytes = BinaryConverter.byteArrayToInt(output, 4);

            if (availableBytes <= returnedBytes) {
               break;
            }

            parameters[0] = new ProgramParameter(availableBytes);
            parameters[1] = new ProgramParameter(BIN4.toBytes(availableBytes));
            pgmCall.setParameterList(parameters);
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException | PropertyVetoException e) {
         e.printStackTrace();
      }

      return new JApiResult(output, pgmCall.getMessageList());
   }
}
