package ch.rasc.otodo;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;

class PlaywrightIT extends AbstractBaseTest {

	private static final DockerImageName NGINX_IMAGE = DockerImageName.parse("nginx:1.30.3-alpine");

	private static final DockerImageName PLAYWRIGHT_IMAGE = DockerImageName
		.parse("mcr.microsoft.com/playwright:v1.61.0-noble");

	@LocalServerPort
	private int localServerPort;

	@TempDir
	private Path tempDir;

	@Test
	void loginFormShowsValidationErrors() throws IOException {
		withPage(page -> {
			page.navigate(appUrl("/login"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

			assertThat(page.getByText("Email is required").isVisible()).isTrue();
			assertThat(page.getByText("Password is required").isVisible()).isTrue();
		});
	}

	@Test
	void userCanLoginThroughBuiltClient() throws IOException {
		withPage(page -> {
			login(page, "user@test.com", "password", "/todos");

			assertThat(page.url()).contains("/todos");
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Todos")).waitFor();
		});
	}

	@Test
	void userCanCreateTodoThroughBuiltClient() throws IOException {
		withPage(page -> {
			login(page, "user@test.com", "password", "/todos");
			page.locator("ion-fab-button").click();
			page.waitForURL("**/todos/edit");

			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save")).click();
			page.getByText("Subject is required").waitFor();

			page.getByLabel("Subject").fill("Playwright smoke todo");
			page.getByLabel("Description").fill("Created through the Testcontainers browser");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Save")).click();
			page.waitForURL("**/todos");

			assertThat(page.getByText("Playwright smoke todo").isVisible()).isTrue();
		});
	}

	@Test
	void adminCanOpenUsersPageThroughBuiltClient() throws IOException {
		withPage(page -> {
			login(page, "admin@test.com", "password", "/users");

			assertThat(page.url()).contains("/users");
			page.getByText("admin@test.com").waitFor();
			page.getByText("user@test.com").waitFor();
		});
	}

	@Test
	void publicRoutesAndSignalFormsWorkThroughBuiltClient() throws IOException {
		withPage(page -> {
			page.navigate(appUrl("/#/signup"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Sign Up")).waitFor();
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up")).click();
			page.getByText("Email is required").waitFor();
			page.getByText("Password is required").waitFor();

			page.navigate(appUrl("/#/password-reset-request"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Request Password Reset")).waitFor();
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Request Password Reset")).click();
			page.getByText("Email is required").waitFor();

			page.navigate(appUrl("/#/signup-confirm/bad-token"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByText("Something went wrong").waitFor();

			page.navigate(appUrl("/#/email-change-confirm/bad-token"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByText("Something went wrong").waitFor();

			page.navigate(appUrl("/#/offline"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Offline")).waitFor();
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Try to reconnect")).waitFor();
		});
	}

	@Test
	void userProfileRoutesAndSignalFormsWorkThroughBuiltClient() throws IOException {
		withPage(page -> {
			login(page, "user@test.com", "password", "/todos");

			page.navigate(appUrl("/#/profile"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Profile")).waitFor();
			page.getByText("Server:").waitFor();
			page.getByText("Client:").waitFor();

			page.locator("ion-button", new Page.LocatorOptions().setHasText("Change my Password")).click();
			page.waitForURL("**/profile/password");
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change Password")).click();
			Locator passwordPage = page.locator("app-password");
			passwordPage.getByText("Old Password is required").waitFor();
			passwordPage.getByText("New Password is required").waitFor();

			page.navigate(appUrl("/#/profile/email"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Change Email")).click();
			Locator emailPage = page.locator("app-email");
			emailPage.getByText("Password is required").waitFor();
			emailPage.getByText("Email is required").waitFor();

			page.navigate(appUrl("/#/profile/sessions"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.locator("ion-title", new Page.LocatorOptions().setHasText("Sessions")).waitFor();
			page.locator("app-sessions").getByText("Last Access:").waitFor();

			page.navigate(appUrl("/#/profile/account"),
					new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Delete my Account")).click();
			page.locator("app-account").getByText("Password is required").waitFor();

			page.navigate(appUrl("/#/logout"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
			page.getByText("You have been successfully logged out.").waitFor();
		});
	}

	private void withPage(PageConsumer consumer) throws IOException {
		Path clientDist = Path.of(System.getProperty("client.dist.dir", "../client/dist/app/browser"))
			.toAbsolutePath()
			.normalize();
		assertThat(clientDist.resolve("index.html")).exists();

		Testcontainers.exposeHostPorts(this.localServerPort);

		try (Network network = Network.newNetwork()) {
			Path nginxConfig = writeNginxConfig();
			Path playwrightConfig = writePlaywrightConfig();

			try (GenericContainer<?> web = new GenericContainer<>(NGINX_IMAGE).withNetwork(network)
				.withNetworkAliases("otodo-web")
				.withExposedPorts(8080)
				.withFileSystemBind(clientDist.toString(), "/usr/share/nginx/html", BindMode.READ_ONLY)
				.withFileSystemBind(nginxConfig.toString(), "/etc/nginx/conf.d/default.conf", BindMode.READ_ONLY)
				.waitingFor(Wait.forHttp("/").forPort(8080));
					GenericContainer<?> playwrightServer = new GenericContainer<>(PLAYWRIGHT_IMAGE).withNetwork(network)
						.withExposedPorts(3000)
						.withFileSystemBind(playwrightConfig.toString(), "/tmp/playwright-config.json",
								BindMode.READ_ONLY)
						.withCommand("npx", "--yes", "playwright@1.61.0", "launch-server", "--browser", "chromium",
								"--config", "/tmp/playwright-config.json")
						.waitingFor(Wait.forLogMessage(".*ws://.*", 1))) {

				web.start();
				playwrightServer.start();

				String wsEndpoint = "ws://" + playwrightServer.getHost() + ":" + playwrightServer.getMappedPort(3000)
						+ "/";
				try (Playwright playwright = Playwright.create();
						Browser browser = playwright.chromium().connect(wsEndpoint)) {
					Page page = browser.newPage(new Browser.NewPageOptions().setViewportSize(1280, 720));
					consumer.accept(page);
				}
			}
		}
	}

	private static String appUrl(String path) {
		return "http://otodo-web:8080" + path;
	}

	private static void login(Page page, String email, String password, String expectedPath) {
		page.navigate(appUrl("/login"), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

		Locator loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
		loginButton.waitFor();

		page.getByLabel("Email").fill(email);
		page.getByLabel("Password").fill(password);
		loginButton.click();
		page.waitForURL("**" + expectedPath);
	}

	private Path writeNginxConfig() throws IOException {
		String config = """
				server {
				    listen 8080;
				    root /usr/share/nginx/html;
				    index index.html;

				    location /be/ {
				        proxy_pass http://host.testcontainers.internal:%d;
				        proxy_http_version 1.1;
				        proxy_set_header Host $host;
				        proxy_set_header X-Real-IP $remote_addr;
				    }

				    location / {
				        try_files $uri $uri/ /index.html;
				    }
				}
				""".formatted(this.localServerPort);

		Path nginxConfig = this.tempDir.resolve("default.conf");
		Files.writeString(nginxConfig, config);
		return nginxConfig;
	}

	private Path writePlaywrightConfig() throws IOException {
		String config = """
				{
				  "port": 3000,
				  "host": "0.0.0.0",
				  "wsPath": "/",
				  "headless": true,
				  "args": ["--no-sandbox"]
				}
				""";

		Path playwrightConfig = this.tempDir.resolve("playwright-config.json");
		Files.writeString(playwrightConfig, config);
		return playwrightConfig;
	}

	@FunctionalInterface
	private interface PageConsumer {

		void accept(Page page);

	}

}
