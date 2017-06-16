package lppedd.j;

import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ErrorCodeParameter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import java.beans.PropertyVetoException;
import java.io.IOException;
import lppedd.j.JUserSpace.JUserSpaceBuilder;
import lppedd.j.abstracts.JAbstractFile;
import lppedd.j.interfaces.AS400DataTypes;
import lppedd.j.interfaces.JBase;
import lppedd.j.interfaces.JFile;
import lppedd.j.interfaces.JMember;
import lppedd.j.interfaces.JObject;

import static com.ibm.as400.access.BinaryConverter.byteArrayToInt;
import static lppedd.j.JConnection.getInstance;
import static lppedd.j.misc.JUtil.getQualifiedPath;
import static lppedd.j.misc.JUtil.getRandomString;
import static lppedd.j.misc.JUtil.newMessage;
import static lppedd.misc.EmptyArrays.EMPTY_BYTE;

/**
 * @author Edoardo Luppi
 */
public final class JAPI implements AS400DataTypes
{
   private static final AS400Message ERROR_MESSAGE = newMessage("ERROR", "Program not called correctly");

   /**
    * Deletes a user space object.
    *
    * @see <a href="https://www.ibm.com/support/knowledgecenter/en/ssw_ibm_i_72/apis/qusdltus.htm">IBM Knowledge Center</a>
    *
    * @param userSpace The user space to delete
    */
   public static boolean QUSDLTUS(final JUserSpace userSpace) {
      final ProgramParameter[] parameters = new ProgramParameter[] {
         new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400(), "/QSYS.LIB/QUSDLTUS.PGM", parameters);

      try {
         if (pgmCall.run()) {
            return pgmCall.getMessageList().length == 0;
         }
      } catch (AS400SecurityException | ErrorCompletingRequestException | IOException | InterruptedException | ObjectDoesNotExistException e) {
         e.printStackTrace();
      }

      return false;
   }

   public static JAPIOutput QSRLSAVF(final String format, final String objectFilter, final String typeFilter, final JSaveFile object) {
      final JUserSpace userSpace = new JUserSpace.JUserSpaceBuilder(getRandomString(10), "QTEMP")
              .length(80000)
              .autoExtendible(true)
              .initialValue((byte) 0x00)
              .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JAPIOutput(ERROR_MESSAGE);
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

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400(), "/QSYS.LIB/QSRLSAVF.PGM", parameters);
      byte[] buffer = EMPTY_BYTE;

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
      return new JAPIOutput(buffer, pgmCall.getMessageList());
   }

   public static JAPIOutput QUSLFLD(final String format, final String recordFormat, final JFile object) {
      final JUserSpace userSpace = new JUserSpace.JUserSpaceBuilder(getRandomString(10), "QTEMP")
              .length(80000)
              .autoExtendible(true)
              .initialValue((byte) 0x00)
              .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JAPIOutput(ERROR_MESSAGE);
      }

      userSpace.setText("QUSLFLD userspace");
      userSpace.commitChanges();

      final ProgramParameter[] parameters = new ProgramParameter[] {
         new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
         new ProgramParameter(CHAR8.toBytes(format)),
         new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
         new ProgramParameter(CHAR10.toBytes(recordFormat)),
         new ProgramParameter(CHAR1.toBytes("0")),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400(), "/QSYS.LIB/QUSLFLD.PGM", parameters);
      byte[] buffer = EMPTY_BYTE;

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
      return new JAPIOutput(buffer, pgmCall.getMessageList());
   }

   public static JAPIOutput QUSLRCD(final String format, final JFile object) {
      final JUserSpace userSpace = new JUserSpaceBuilder(getRandomString(10), "QTEMP")
              .length(20000)
              .autoExtendible(true)
              .initialValue((byte) 0x00)
              .build();

      userSpace.delete();

      if (!userSpace.create()) {
         return new JAPIOutput(ERROR_MESSAGE);
      }

      userSpace.setText("QUSLRCD userspace");
      userSpace.commitChanges();

      final ProgramParameter[] parameters = new ProgramParameter[] {
         new ProgramParameter(CHAR20.toBytes(userSpace.getQualifiedPath())),
         new ProgramParameter(CHAR8.toBytes(format)),
         new ProgramParameter(CHAR20.toBytes(object.getQualifiedPath())),
         new ProgramParameter(CHAR1.toBytes("0")),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400(), "/QSYS.LIB/QUSLRCD.PGM", parameters);
      byte[] buffer = EMPTY_BYTE;

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
      return new JAPIOutput(buffer, pgmCall.getMessageList());
   }

   public static JAPIOutput QCLRPGMI(final String format, final JObject object) {
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

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QCLRPGMI.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = byteArrayToInt(output, 0);
            final int availableBytes = byteArrayToInt(output, 4);

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

      return new JAPIOutput(output, pgmCall.getMessageList());
   }

   public static JAPIOutput QUSROBJD(final String format, final JObject object) {
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

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QUSROBJD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = byteArrayToInt(output, 0);
            final int availableBytes = byteArrayToInt(output, 4);

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

      return new JAPIOutput(output, pgmCall.getMessageList());
   }

   public static JAPIOutput QDBRTVFD(final String format, final JAbstractFile file) {
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

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QDBRTVFD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = byteArrayToInt(output, 0);
            final int availableBytes = byteArrayToInt(output, 4);

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

      return new JAPIOutput(output, pgmCall.getMessageList());
   }

   public static JAPIOutput QUSRMBRD(final String format, final JBase base) {
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
         new ProgramParameter(CHAR20.toBytes(getQualifiedPath(base.getLibrary(), object))),
         new ProgramParameter(CHAR10.toBytes(member)),
         new ProgramParameter(CHAR1.toBytes("1")),
         new ErrorCodeParameter()
      };

      final ProgramCall pgmCall = new ProgramCall(getInstance().getAS400());
      byte[] output = new byte[0];

      try {
         pgmCall.setProgram("/QSYS.LIB/QUSRMBRD.PGM", parameters);

         while (pgmCall.run()) {
            output = parameters[0].getOutputData();
            final int returnedBytes = byteArrayToInt(output, 0);
            final int availableBytes = byteArrayToInt(output, 4);

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

      return new JAPIOutput(output, pgmCall.getMessageList());
   }
}