package methods;

//Slightly modified method I found online, to simplify the process of reverting a "deepToString" method of a multidimensional array, which the simplifies rendering method in the client
public class CopiedMethods {

    public static Byte[][] stringToDeep(String str) {
        int row = 0;
        int col = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '[') {
                row++;
            }
        }
        row--;
        for (int i = 0;; i++) {
            if (str.charAt(i) == ',') {
                col++;
            }
            if (str.charAt(i) == ']') {
                break;
            }
        }
        col++;

        Byte[][] out = new Byte[row][col];

        str = str.replaceAll("\\[", "").replaceAll("\\]", "");

        String[] s1 = str.split(", ");

        int j = -1;
        for (int i = 0; i < s1.length; i++) {
            if (i % col == 0) {
                j++;
            }
            out[j][i % col] = Byte.parseByte(s1[i]);
        }
        return out;
    }
}
