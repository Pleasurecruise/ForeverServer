const puppeteer = require('puppeteer');

(async () => {
    const url = process.argv[2];
    const screenshotPath = process.argv[3];
    const browser = await puppeteer.launch({ headless: true });
    const page = await browser.newPage();
    await page.setRequestInterception(true);
    page.on('request', request => {
        if (request.url().includes('uni_forceverify_wrapper')) {
            request.abort();
        } else {
            request.continue();
        }
    });
    await page.goto(url, { waitUntil: 'networkidle2' });
    await page.screenshot({ path: screenshotPath });
    await browser.close();
})();