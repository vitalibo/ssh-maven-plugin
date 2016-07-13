package com.epam.maven.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;

/**
 * @author Vitaliy Boyarsky
 */
public class SecureCopyProtocol {

    private SecureCopyProtocol() {
        super();
    }

    /**
     * http://www.jcraft.com/jsch/examples/ScpTo.java.html
     */
    public static void copyToRemote(Session session, String lfile, String rfile) throws JSchException, IOException {
        // exec 'scp -t rfile' remotely
        String command = "scp -p -t " + rfile;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // Get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();
        if (checkAck(in) != 0) throw new SCPException();

        File _lfile = new File(lfile);
        command = "T" + (_lfile.lastModified() / 1000) + " 0 " + (_lfile.lastModified() / 1000) + " 0\n";
        // The access time should be sent here,
        // But it is not accessible with JavaAPI ;-<
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) throw new SCPException();

        // Send "C0644 filesize filename", where filename should not include '/'
        long fileSize = _lfile.length();
        command = "C0644 " + fileSize + " ";
        String lfileName = lfile;
        lfile = lfile.replace('/', '\\');
        if (lfile.lastIndexOf('\\') > 0)
            lfileName = lfile.substring(lfile.lastIndexOf('\\') + 1);
        command += lfileName;
        command += "\n";
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) throw new SCPException();

        // Send a content of lfile
        FileInputStream fis = new FileInputStream(lfile);
        Reporter reporter = new Reporter(lfileName, fileSize);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0) break;
            reporter.report(len);
            out.write(buf, 0, len);
        }
        reporter.report();
        System.out.println();
        fis.close();

        // Send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();
        if (checkAck(in) != 0) throw new SCPException();

        out.close();
        channel.disconnect();
    }

    /**
     * http://www.jcraft.com/jsch/examples/ScpFrom.java.html
     */
    public static void copyToLocal(Session session, String lfile, String rfile) throws JSchException, IOException {
        // exec 'scp -f rfile' remotely
        String command = "scp -f " + rfile;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // Get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();
        channel.connect();

        String prefix = null;
        if (new File(lfile).isDirectory()) {
            prefix = lfile + File.separator;
        }
        byte[] buf = new byte[1024];

        // Send '\0'
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        while (true) {
            int c = checkAck(in);
            if (c != 'C') break;
            // Read '0644 '
            in.read(buf, 0, 5);
            long filesize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) break;
                if (buf[0] == ' ') break;
                filesize = filesize * 10L + (long) (buf[0] - '0');
            }
            String file;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }
            // Send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // Read a content of lfile
            FileOutputStream fos = new FileOutputStream(prefix == null ? lfile : prefix + file);
            String lfileName = rfile;
            String source = rfile.replace('/', '\\');
            if (source.lastIndexOf('\\') > 0)
                lfileName = source.substring(source.lastIndexOf('\\') + 1);

            Reporter reporter = new Reporter(lfileName, filesize);
            int foo;
            while (true) {
                if (buf.length < filesize) foo = buf.length;
                else foo = (int) filesize;
                foo = in.read(buf, 0, foo);
                reporter.report(foo);
                if (foo < 0) break;
                fos.write(buf, 0, foo);
                filesize -= foo;
                if (filesize == 0L) break;
            }
            reporter.report();
            fos.close();
            if (checkAck(in) != 0) throw new SCPException();

            // Send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
        }
    }

    private static int checkAck(InputStream input) throws IOException {
        int b = input.read();
        if (b == 0) return 0; // success
        if (b == -1) return -1;
        if (b == 1 || b == 2) {
            StringBuilder sb = new StringBuilder();
            int c;
            do {
                c = input.read();
                sb.append((char) c);
            } while (c != '\n');
            throw new SCPException(sb.toString());
        }
        return b;
    }

    private static class Reporter {

        private static final int SIZE_OF_SCALE_GRADUATION = 25;
        private static final int MAX_FILE_NAME_LENGTH = 35;

        private String fname;
        private double division;
        private long copy = 0;
        private int progress = -1;

        Reporter(String fname, long fsize) {
            this.fname = fname.substring(0, fname.length() > MAX_FILE_NAME_LENGTH ? MAX_FILE_NAME_LENGTH : fname.length());
            this.division = 1.0 * fsize / SIZE_OF_SCALE_GRADUATION;
        }

        private static String scaleGraduation(int percentage) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            for (int i = 0; i < SIZE_OF_SCALE_GRADUATION; i++) {
                sb.append(i < percentage ? '#' : '-');
            }
            sb.append(']');
            return sb.toString();
        }

        void report() {
            String sizeFormat = String.format("%8d%s", copy / 1024 == 0 ? copy :
                    copy / 1024, copy / 1024 == 0 ? "B" : "KB");
            System.out.printf("%-35s %s %3d%% %s\r", fname, scaleGraduation(progress),
                    progress * 100 / SIZE_OF_SCALE_GRADUATION, sizeFormat);
        }

        void report(long bytes) {
            copy += bytes;
            int nProgress = (int) (copy / division);
            if (nProgress > progress) {
                progress = nProgress;
                report();
            }
        }

    }

    public static class SCPException extends IOException {

        SCPException() {
            super();
        }

        SCPException(String message) {
            super(message);
        }

    }

}
