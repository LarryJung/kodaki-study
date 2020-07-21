import java.util.ArrayList;
import java.util.List;

public class Variance {

    public static void main(String[] args) {
         List<String> aa = new ArrayList<String>();
         List<Object> bb = new ArrayList<Object>();
         bb.addAll(aa); // 이 아이들을 안전하게 받아줄 수 있다.
        // producing 할 때 cast하면 되니!

        // 이 것은 함부로 받아주었다가 producing 할 때 casting fail 이 난다.
        // aa.addAll(bb);
    }
}
