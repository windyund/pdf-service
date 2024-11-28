package com.smart.neural.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
public class ChromeService implements InitializingBean, DisposableBean {

    private final Integer chromePoolSize = 4;
    private final Deque<Playwright> playwrights = new ArrayDeque<>();
    private final Deque<Browser> browsers = new ArrayDeque<>();
    private final Deque<BrowserContext> browserContexts = new ArrayDeque<>();
    private final Semaphore semaphore = new Semaphore(chromePoolSize);


    public BrowserContext getBrowserContext() throws InterruptedException {
        semaphore.acquire();
        try {
            BrowserContext browserContext;
            synchronized (browserContexts) {
                browserContext = browserContexts.poll();
            }
            if (browserContext == null) {
                // 处理无可用上下文的情况
                log.warn("No available browser context. Creating a new one.");
                return createNewBrowserContext();
            }
            return browserContext;
        } catch (Exception e) {
            log.error("Error getting browser context: ", e);
            semaphore.release();
            throw e;
        }
    }

    private BrowserContext createNewBrowserContext() {
        try {
            Playwright playwright;
            synchronized (playwrights) {
                playwright = playwrights.poll();
            }
            if (playwright == null) {
                log.warn("No available playwright. Creating a new one.");
                playwright = createNewPlaywright();
            }
            Browser browser;
            synchronized (browsers) {
                browser = browsers.poll();
            }
            if (browser == null) {
                log.warn("No available browser. Creating a new one.");
                browser = createNewBrowser(playwright);
            }
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080);
            newContextOptions.setDeviceScaleFactor(2.0);
            return browser.newContext(newContextOptions);
        } catch (Exception e) {
            log.error("Error creating new browser context: ", e);
            throw e;
        }
    }

    private Playwright createNewPlaywright() {
        try {
            Playwright playwright = Playwright.create();
            playwrights.offer(playwright);
            return playwright;
        } catch (Exception e) {
            log.error("Error creating new playwright: ", e);
            throw e;
        }
    }

    private Browser createNewBrowser(Playwright playwright) {
        try {
            Browser browser = playwright.chromium().launch(getLaunchOptions());
            browsers.offer(browser);
            return browser;
        } catch (Exception e) {
            log.error("Error creating new browser: ", e);
            throw e;
        }
    }

    public void returnBrowserContext(BrowserContext browserContext) {
        if (browserContext == null) {
            log.warn("Attempted to return a null browser context.");
            return;
        }
        try {
            synchronized (browserContexts) {
                browserContexts.offer(browserContext);
            }
        } finally {
            semaphore.release();
        }
    }


    private BrowserType.LaunchOptions getLaunchOptions() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setArgs(Arrays.asList(
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--disable-setuid-sandbox",
                "--no-first-run",
                "--no-zygote",
                "--disable-web-security",
                "--no-sandbox"
        ));
        launchOptions.setTimeout(0);
        launchOptions.setHeadless(true);
        launchOptions.setHandleSIGHUP(false);
        launchOptions.setHandleSIGINT(false);
        launchOptions.setHandleSIGTERM(false);
        return launchOptions;
    }


    @Override
    public void afterPropertiesSet() {
        try {
            for (int i = 0; i < chromePoolSize; i++) {
                Playwright playwright = Playwright.create();
                playwrights.offer(playwright);
                Browser browser = playwright.chromium().launch(getLaunchOptions());
                // 设置分辨率和缩放比例
                Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080);
                // 增加分辨率会消耗更多的资源，并可能增加截图的时间
                newContextOptions.setDeviceScaleFactor(2.0);
                BrowserContext browserContext = browser.newContext(newContextOptions);
                browserContexts.offer(browserContext);
                browsers.offer(browser);
            }
        } catch (Exception e) {
            throw new RuntimeException("Playwright init error: ", e);
        }
    }


    @Override
    public void destroy() {
        synchronized (browserContexts) {
            if (!browserContexts.isEmpty()) {
                browserContexts.forEach(BrowserContext::close);
                browserContexts.clear();
            }
        }
        synchronized (browsers) {
            if (!browsers.isEmpty()) {
                browsers.forEach(Browser::close);
                browsers.clear();
            }
        }
        synchronized (playwrights) {
            if (!playwrights.isEmpty()) {
                playwrights.forEach(Playwright::close);
                playwrights.clear();
            }
        }
        log.info("Playwright resources have been successfully released.");
    }
}
