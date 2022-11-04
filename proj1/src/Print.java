public class Print {
    
    public static void red(String msg) {
        System.out.println(Color.RED + msg + Color.RESET);
    }

    public static void yellow(String msg) {
        System.out.println(Color.YELLOW + msg + Color.RESET);
    }

    public static void blue(String msg) {
        System.out.println(Color.BLUE + msg + Color.RESET);
    }

    public static void green(String msg) {
        System.out.println(Color.GREEN + msg + Color.RESET);
    }

    public static void magenta(String msg) {
        System.out.println(Color.MAGENTA + msg + Color.RESET);
    }

    public static void cyan(String msg) {
        System.out.println(Color.CYAN + msg + Color.RESET);
    }

    public static void white(String msg) {
        System.out.println(Color.WHITE + msg + Color.RESET);
    }

    public static void error(String msg) {
        System.out.println(Color.RED_BOLD_BRIGHT + msg + Color.RESET);
    }

    public static void system(String msg) {
        System.out.println(Color.WHITE_BOLD_BRIGHT + msg + Color.RESET);
    }

}
