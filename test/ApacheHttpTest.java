import jp319.zerochan.utils.statics.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class ApacheHttpTest {
	public static void main(String[] args) {
		CloseableHttpClient httpClient = HttpClients.createMinimal();
		HttpClientContext context =HttpClientContext.create();
		
		String url = Constants.API_BASE_URL + "/genshin?json&"+null;
		HttpGet httpGet =new HttpGet(url);
		httpGet.addHeader("User-Agent",                 Constants.USER_AGENT_HEADER);
		httpGet.addHeader("Accept",                     Constants.ACCEPT_HEADER);
		httpGet.addHeader("Accept-Language",            Constants.ACCEPT_LANGUAGE_HEADER);
		httpGet.addHeader("Cookie",                     Constants.COOKIE_HEADER);
		
		System.out.println("Current Link: " + url);
		
		try {
			CloseableHttpResponse response = httpClient.execute(httpGet, context);
			HttpEntity entity = response.getEntity();
			int statusCode = response.getStatusLine().getStatusCode();
			
			System.out.println("Response Code: " + statusCode);
			
			if (statusCode >= 300 && statusCode < 400) {
				String newLocation = response.getFirstHeader("Location").getValue();
				String newLink = Constants.API_BASE_URL + newLocation + "?json&";
				
				System.out.println("New Location: " + newLocation);
				System.out.println("New Link: " + newLink);
				
				httpGet.setURI(URI.create(newLink));
				response = httpClient.execute(httpGet, context);
				entity = response.getEntity();
				
				System.out.println("New Response Code: " + response.getStatusLine().getStatusCode());
			}
			//System.out.println("Response Encoding: " + response.getFirstHeader("Content-Encoding").getValue());
			System.out.println("Response Body: " + EntityUtils.toString(entity, StandardCharsets.UTF_8));
			
			EntityUtils.consume(entity);
			response.close();
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
