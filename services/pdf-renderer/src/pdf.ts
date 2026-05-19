import { chromium, type Browser } from "playwright";

let browser: Browser | undefined;

const getBrowser = async () => {
  browser ??= await chromium.launch({ headless: true });
  return browser;
};

export const renderPdf = async (html: string) => {
  const activeBrowser = await getBrowser();
  const page = await activeBrowser.newPage();

  try {
    await page.setContent(html, { waitUntil: "networkidle" });
    return await page.pdf({
      format: "A4",
      printBackground: true,
      preferCSSPageSize: true,
    });
  } finally {
    await page.close();
  }
};

export const closeBrowser = async () => {
  if (!browser) {
    return;
  }

  await browser.close();
  browser = undefined;
};
