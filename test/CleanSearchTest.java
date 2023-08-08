import jp319.zerochan.controllers.Search;
import jp319.zerochan.utils.sanitations.CleanSearchResult;

public class CleanSearchTest {
	public static void main(String[] args) {
		String tags = "Genshin+Impact";
		String result = CleanSearchResult.clean(new Search(tags,"p=1&l=1").getResult());
		System.out.println("Cleaned Result: " + result);
	}
}
