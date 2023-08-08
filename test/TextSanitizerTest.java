import static jp319.zerochan.utils.sanitations.SanitizeText.sanitizeTextForLink;

public class TextSanitizerTest {
    public static void main(String[] args) {
        String inputText = "Fate/Stay night: unlimited blade works, Tohsaka Rin";
        String sanitizedText = sanitizeTextForLink(inputText);
        System.out.println(sanitizedText); // Output: Fate%2FStay+night%3A+unlimited+blade+works
    }
}
