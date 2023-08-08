import jp319.zerochan.controllers.Search;

public class SearchTest {
	public static void main(String[] args) {
		String id = "3990743";
		String tags = "fate+stay+night,genshin";
		String filters = "Strict&p=1&l=1";
//		System.out.println("Showing Results for: " + id);
//		System.out.println(new Search(id).getResult());

		System.out.println("==========================================");
		
		Search tagSearchResult = new Search(tags, filters);
		System.out.println("Showing Results for: " + tags + " & " + filters);
		System.out.println(tagSearchResult.getResult());
	}
}
