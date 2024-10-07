import puppeteer from 'puppeteer';

(async () => {
    const url = process.argv[2];
    const screenshotPath = process.argv[3];
    const browser = await puppeteer.launch({ headless: true });
    const page = await browser.newPage();
    let forceVerifyDisabled = false;

    try {
        await page.setRequestInterception(true);
        page.on('request', request => {
            if (request.url().includes('uni_forceverify_wrapper')) {
                request.abort();
                forceVerifyDisabled = true;
            } else {
                request.continue();
            }
        });

        await page.goto(url, { waitUntil: 'networkidle2' });

        if (forceVerifyDisabled) {
            console.log('uni_forceverify_wrapper was disabled');
        }

        await page.screenshot({ path: screenshotPath });
        console.log(`Screenshot saved at ${screenshotPath}`);
    } catch (error) {
        console.error('Error taking screenshot:', error);
    } finally {
        await browser.close();
    }
})();