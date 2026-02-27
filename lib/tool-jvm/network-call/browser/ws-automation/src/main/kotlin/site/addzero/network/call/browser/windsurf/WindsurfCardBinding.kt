package site.addzero.network.call.browser.windsurf

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.AriaRole
import java.util.regex.Pattern

/**
 * Windsurf Pro 绑卡自动化步骤
 *
 * 基于 Playwright Inspector 录制的操作流程：
 * 1. 点击头像 → Profile
 * 2. Upgrade to Pro → Select plan（$15/mo）
 * 3. 填写支付表单（卡号、有效期、CVC、姓名、地址）
 * 4. 提交
 */
object WindsurfCardBinding {

  /**
   * 执行完整的绑卡流程
   *
   * @param page     已登录的 Windsurf 页面
   * @param card     支付卡信息（为 null 时自动生成随机卡信息）
   */
  fun bindCard(page: Page, card: WindsurfCardInfo? = null) {
    val cardInfo = card ?: WindsurfCardGenerator.generate()
    println("[WindsurfCardBinding] starting card binding with card: ${cardInfo.cardNumber}")

    navigateToUpgrade(page)
    selectProPlan(page)
    fillPaymentForm(page, cardInfo)
    submitPayment(page)

    println("[WindsurfCardBinding] card binding completed")
  }

  /**
   * 导航到 Profile → Upgrade to Pro
   */
  private fun navigateToUpgrade(page: Page) {
    println("[WindsurfCardBinding] navigating to profile...")

    // 点击头像按钮（文本为 "A" 的按钮，通常是用户名首字母）
    val avatarBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("A").setExact(true))
    avatarBtn.waitFor()
    avatarBtn.click()
    println("[WindsurfCardBinding] clicked avatar button")

    // 点击 Profile 链接
    val profileLink = page.getByRole(AriaRole.LINK, Page.GetByRoleOptions().setName("Profile"))
    profileLink.waitFor()
    profileLink.click()
    println("[WindsurfCardBinding] clicked Profile link")

    // 点击 Upgrade to Pro
    val upgradeBtn = page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Upgrade to Pro"))
    upgradeBtn.waitFor()
    upgradeBtn.click()
    println("[WindsurfCardBinding] clicked Upgrade to Pro")
  }

  /**
   * 选择 Pro 计划（$15/mo）
   */
  private fun selectProPlan(page: Page) {
    println("[WindsurfCardBinding] selecting Pro plan...")

    val planDiv = page.locator("div")
      .filter(Locator.FilterOptions().setHasText(Pattern.compile("^ProPopular\\$15per monthSelect plan$")))
    val selectBtn = planDiv.getByRole(AriaRole.BUTTON)
    selectBtn.waitFor()
    selectBtn.click()
    println("[WindsurfCardBinding] selected Pro $15/mo plan")
  }

  /**
   * 填写 Stripe 支付表单
   */
  private fun fillPaymentForm(page: Page, card: WindsurfCardInfo) {
    println("[WindsurfCardBinding] filling payment form...")

    // 展开银行卡表单（Stripe Accordion 默认可能折叠，需要点击 radio/按钮展开）
    expandCardAccordion(page)

    // 等待支付表单加载
    val cardInput = page.getByPlaceholder("1234 1234 1234")
    cardInput.waitFor()

    // 卡号
    cardInput.click()
    cardInput.fill(card.cardNumber)
    println("[WindsurfCardBinding] filled card number: ${card.cardNumber}")

    // 有效期
    cardInput.press("Tab")
    val expiryInput = page.getByPlaceholder("月份/年份")
    expiryInput.fill(card.expiry)
    println("[WindsurfCardBinding] filled expiry: ${card.expiry}")

    // CVC
    expiryInput.press("Tab")
    val cvcInput = page.getByPlaceholder("CVC")
    cvcInput.fill(card.cvc)
    println("[WindsurfCardBinding] filled CVC: ${card.cvc}")

    // 持卡人姓名
    cvcInput.press("Tab")
    val nameInput = page.getByPlaceholder("全名")
    nameInput.click()
    nameInput.fill(card.holderName)
    println("[WindsurfCardBinding] filled holder name: ${card.holderName}")

    // 国家
    page.getByLabel("国家或地区").selectOption(card.country)
    println("[WindsurfCardBinding] selected country: ${card.country}")

    // 邮编
    val postalInput = page.getByPlaceholder("邮编")
    postalInput.click()
    postalInput.fill(card.postalCode)
    println("[WindsurfCardBinding] filled postal code: ${card.postalCode}")

    // 省/州
    page.getByLabel("省/州").selectOption(card.province)
    println("[WindsurfCardBinding] selected province: ${card.province}")

    // 区/县
    val districtInput = page.getByPlaceholder("地区")
    districtInput.click()
    districtInput.fill(card.district)
    println("[WindsurfCardBinding] filled district: ${card.district}")

    // 地址第 1 行
    val addr1Input = page.getByPlaceholder("地址第 1 行")
    addr1Input.click()
    addr1Input.fill(card.addressLine1)
    println("[WindsurfCardBinding] filled address line 1: ${card.addressLine1}")

    // 地址第 2 行（可选）
    if (card.addressLine2 != null) {
      val addr2Input = page.getByPlaceholder("地址第 2 行")
      addr2Input.click()
      addr2Input.fill(card.addressLine2)
      println("[WindsurfCardBinding] filled address line 2: ${card.addressLine2}")
    }
  }

  /**
   * 展开 Stripe 银行卡 Accordion（默认可能折叠）
   *
   * Stripe 支付表单使用 Accordion 组件，银行卡选项需要点击 radio/button 展开。
   * 结构：`#payment-form .PaymentMethodFormAccordion > div:nth-child(1)` 内的 button
   */
  private fun expandCardAccordion(page: Page) {
    println("[WindsurfCardBinding] expanding card accordion...")

    // 等待支付表单加载
    page.locator("#payment-form").waitFor()
    Thread.sleep(1_000)

    // 策略1：点击 Accordion 第一项的 button（银行卡 radio）
    val accordionBtn = page.locator(
      "#payment-form .PaymentMethodFormAccordion > div:nth-child(1) button"
    )
    if (runCatching { accordionBtn.count() }.getOrDefault(0) > 0) {
      runCatching {
        accordionBtn.first().click(Locator.ClickOptions().setTimeout(3000.0))
        println("[WindsurfCardBinding] clicked card accordion button")
        Thread.sleep(500)
        return
      }
    }

    // 策略2：点击 AccordionItemCover 的 button
    val coverBtn = page.locator(".AccordionItemCover-actionContainer button")
    if (runCatching { coverBtn.count() }.getOrDefault(0) > 0) {
      runCatching {
        coverBtn.first().click(Locator.ClickOptions().setTimeout(3000.0))
        println("[WindsurfCardBinding] clicked AccordionItemCover button")
        Thread.sleep(500)
        return
      }
    }

    // 策略3：直接点击第一个 Accordion div（整个区域可点击）
    val firstAccordion = page.locator(
      "#payment-form .PaymentMethodFormAccordion > div:nth-child(1)"
    )
    if (runCatching { firstAccordion.count() }.getOrDefault(0) > 0) {
      runCatching {
        firstAccordion.first().click(Locator.ClickOptions().setTimeout(3000.0))
        println("[WindsurfCardBinding] clicked first accordion div")
        Thread.sleep(500)
        return
      }
    }

    println("[WindsurfCardBinding] card accordion may already be expanded, proceeding...")
  }

  /**
   * 提交支付
   */
  private fun submitPayment(page: Page) {
    println("[WindsurfCardBinding] submitting payment...")
    val submitBtn = page.getByTestId("hosted-payment-submit-button")
    submitBtn.waitFor()
    submitBtn.click()
    println("[WindsurfCardBinding] payment submitted, waiting for confirmation...")

    // 等待提交处理（可能需要一些时间）
    Thread.sleep(5_000)
  }
}
