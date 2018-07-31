package cn.edu.sjtu.sshhsh;

public class Main {
    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8000;
        }
        new UDPServer(port).run();
    }
}
