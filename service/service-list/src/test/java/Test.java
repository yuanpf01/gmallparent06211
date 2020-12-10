/**
 * @author yuanpf
 * @create 2020-12-09 22:13
 */
public class Test {
    public static void main(String[] args) {
        int[] a = new int[]{12, 25, 36, -8};
        selectSort(a);
        for (int i : a) {
            System.out.println(i);
        }
    }

    public static void selectSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            //设定基准位置
            int index = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[index]) {
                    index = j;
                }
            }
            int max = a[i];
            a[i] = a[index];
            a[index] = max;
        }
    }
}
