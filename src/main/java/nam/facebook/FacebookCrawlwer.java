package nam.facebook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class FacebookCrawlwer {
	public static void main(String[] args) throws InterruptedException {
		// Create a new instance of the Chrome
		// Notice that the remainder of the code relies on the interface,
		// not the implementation.
		WebDriver driver = new ChromeDriver();

		driver.get("http:\\www.facebook.com");

		WebElement element1 = driver.findElement(By.id("email"));
		element1.sendKeys("****");

		WebElement element2 = driver.findElement(By.id("pass"));
		element2.sendKeys("*****");

		WebElement element3 = driver.findElement(By.id("loginbutton"));
		element3.click();
		WebElement profile = driver.findElement(By.xpath("//*[@id=\"u_0_a\"]/div[1]/div[1]/div/a"));
		String profileUrl = profile.getAttribute("href");
		if (StringUtils.isEmpty(profileUrl)) {
			quit(driver);
		}
		 Set<String> fbUrls = collectFriendsByDeep(profileUrl, driver, 1);
		 List<User> fbUsers = collectFbUsersData(fbUrls, driver);
//		Set<String> fbUrls = new HashSet<>();
//		fbUrls.add(profileUrl);
//		List<User> fbUsers = collectFbUsersData(fbUrls, driver);
		System.out.println("Page title is: " + driver.getTitle());
		// Close the browser
		driver.quit();
	}

	private static List<User> collectFbUsersData(Set<String> fbUrls, WebDriver driver) {
		List<User> users = new ArrayList<>();
		for (String url : fbUrls) {
			users.add(collectFbUserData(url, driver));
		}
		return users;
	}

	private static User collectFbUserData(String profileUrl, WebDriver driver) {
		if (profileUrl.contains("id")) {
			driver.get(profileUrl + "&sk=music");
		} else {
			driver.get(profileUrl + "/music");
		}
		List<WebElement> musicList = driver
				.findElements(By.xpath("//div[starts-with(@id,\"pagelet_timeline_app_collection_\")]/ul/li[*]/div"));
		List<Music> musicListData = new ArrayList();
		int i = 1;
		for (WebElement music : musicList) {
			WebElement name = music.findElement(
					By.xpath("//div[starts-with(@id,\"pagelet_timeline_app_collection_\")]/ul/li[*]/div/div[1]/a"));
			Music musicData = new Music();
			musicData.setName(name.getText());
			musicData.setUrl(name.getAttribute("href"));
			WebElement type = music.findElement(
					By.xpath("//div[starts-with(@id,\"pagelet_timeline_app_collection_\")]/ul/li[*]/div[1]/div"));
			musicData.setType(type.getText());
			musicListData.add(musicData);
			i++;
		}
		User userData = new User();

		WebElement user = driver.findElement(By.xpath("//*[@id=\"fb-timeline-cover-name\"]/a"));
		userData.setMusics(musicListData);
		userData.setName(user.getText());
		userData.setUrl(user.getAttribute("href"));
		return userData;

	}

	static Set<String> collectFriendsByDeep(String profileUrl, WebDriver driver, int deep) {
		int currentDeep = 0;
		Set<String> fbUrls = new HashSet<String>();
		Set<String> leaves = new HashSet<String>();
		Set<String> accUrls = collectFriends(profileUrl, driver);
		while (currentDeep < deep) {
			for (String friendUrl : accUrls) {
				leaves.addAll(collectFriends(friendUrl, driver));
			}
			leaves.removeAll(fbUrls);
			fbUrls.addAll(leaves);
			fbUrls.addAll(accUrls);
			accUrls = leaves;
			System.out.println("deep = " + currentDeep);
			System.out.println("size of leaf = " + leaves.size());
			System.out.println("size of total = " + fbUrls.size());
			leaves = new HashSet<String>();

			currentDeep++;
		}

		return fbUrls;

	}

	static Set<String> collectFriends(String profileUrl, WebDriver driver) {
		Set<String> accUrls = new HashSet<String>();
		if (profileUrl.contains("id")) {
			driver.get(profileUrl + "&sk=friends");
		} else {
			driver.get(profileUrl + "/friends");
		}
		List<WebElement> friends = driver.findElements(By.xpath(
				"//div[starts-with(@id,\"pagelet_timeline_app_collection_\")]/ul[*]/li[*]/div/div/div[2]/div/div[2]/div/a"));
		System.out.println("friend number: " + friends.size());
		for (WebElement friend : friends) {
			String friendUrl = friend.getAttribute("href");
			if (friendUrl.contains("&amp")) {
				friendUrl = friendUrl.substring(0, friendUrl.indexOf("&amp"));
			} else {
				friendUrl = friendUrl.substring(0, friendUrl.indexOf("fref") - 1);
			}
			accUrls.add(friendUrl);
		}
		return accUrls;
	}

	private static void quit(WebDriver driver) {
		driver.quit();
	}
}
