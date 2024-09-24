package better.life.autoquiet.Sub;

import java.text.Normalizer;

public class AddSuffixStr {
    public String add(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }
}
