//> using scala 3.7.4
//> using platform scala-js

//> using dep "org.scala-js::scalajs-dom::2.8.1"
//> using dep "com.raquo::laminar::17.2.1"

//> using jsModuleKind es

import org.scalajs.dom
import org.scalajs.dom.{
  Element,
  HTMLElement,
  IntersectionObserver,
  IntersectionObserverInit
}

import com.raquo.laminar.api.L.*
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// Data model for sections
case class SubSection(title: String, content: List[String])
case class Section(title: String, subSections: List[SubSection])

val sectionData: List[Section] = List(
  Section(
    "Section 1: Getting Started",
    List(
      SubSection(
        "Introduction",
        List(
          "Welcome to our comprehensive guide!",
          "Our platform is designed to help developers build amazing applications.",
          "Whether you're a beginner or experienced, you'll find everything you need.",
          "This guide will walk you through all the essential concepts.",
          "Let's dive in and explore what makes our platform unique."
        )
      ),
      SubSection(
        "Installation",
        List(
          "Follow these steps to install the software on your machine.",
          "Step 1: Download the installer from our official website.",
          "Step 2: Run the installer and follow the on-screen instructions.",
          "Step 3: Choose your installation directory.",
          "Step 4: Wait for the installation to complete.",
          "Step 5: Verify the installation by running the version check command."
        )
      ),
      SubSection(
        "Configuration",
        List(
          "Configure your settings by editing the config file.",
          "The configuration file is located in your home directory.",
          "You can modify database connections, API endpoints, and more.",
          "We recommend starting with the default configuration.",
          "Always backup your configuration file before making changes.",
          "Restart the application after making changes."
        )
      )
    )
  ),
  Section(
    "Section 2: Advanced Features",
    List(
      SubSection(
        "API Integration",
        List(
          "Learn how to integrate with our API.",
          "Our API supports JSON and XML formats.",
          "Authentication is handled through OAuth 2.0 tokens.",
          "Rate limiting is applied to ensure fair usage.",
          "We provide SDKs for popular programming languages.",
          "All API calls are logged and can be monitored."
        )
      ),
      SubSection(
        "Custom Plugins",
        List(
          "Create your own plugins to extend functionality.",
          "Plugins can hook into various lifecycle events.",
          "We provide a plugin development kit.",
          "Published plugins can be shared in our marketplace.",
          "Version management ensures compatibility.",
          "Debug mode provides detailed logging."
        )
      ),
      SubSection(
        "Performance Optimization",
        List(
          "Optimize your application for maximum performance.",
          "Enable caching to reduce database queries.",
          "Use lazy loading for resources not immediately needed.",
          "Minimize network requests by bundling assets.",
          "Monitor performance metrics through our dashboard.",
          "Consider horizontal scaling for high-traffic applications."
        )
      )
    )
  ),
  Section(
    "Section 3: Troubleshooting",
    List(
      SubSection(
        "Common Issues",
        List(
          "Here are solutions to frequently encountered problems.",
          "Issue: Connection timeout - Check network settings.",
          "Issue: Authentication failed - Verify credentials.",
          "Issue: Missing dependencies - Run dependency check.",
          "Issue: Slow performance - Review optimization guide.",
          "Issue: Data corruption - Restore from backup."
        )
      ),
      SubSection(
        "Error Messages",
        List(
          "A comprehensive list of error messages and fixes.",
          "ERR001: Invalid configuration - Check config syntax.",
          "ERR002: Database connection failed - Verify server.",
          "ERR003: Permission denied - Check permissions.",
          "ERR004: Resource not found - Check resource location.",
          "ERR005: Rate limit exceeded - Wait for cooldown."
        )
      ),
      SubSection(
        "Getting Help",
        List(
          "Contact our support team or visit community forums.",
          "Our support team is available 24/7 for enterprise.",
          "Community forums are great for finding solutions.",
          "Check our knowledge base for detailed articles.",
          "Submit bug reports through our issue tracker.",
          "Join our Discord server for real-time chat."
        )
      )
    )
  )
)

// Track stuck state for smart shadow logic
var stuckState: Map[Element, Boolean] = Map.empty

// Helper to toggle shadow classes
def setShadow(el: HTMLElement, stuck: Boolean, isList: Boolean): Unit = {
  val shadowClass = if (isList) "shadow-md" else "shadow"
  if (stuck) el.classList.add(shadowClass)
  else el.classList.remove(shadowClass)
}

def updateShadows(listItem: Element): Unit = {
  val listTitle = listItem.querySelector(".list-title")
  val stuckSubTitles = listItem.querySelectorAll(".sub-title.is-stuck")

  if (listTitle != null && stuckState.getOrElse(listTitle, false)) {
    // List title only gets shadow if no sub-titles are stuck
    val shouldHaveShadow = stuckSubTitles.length == 0
    listTitle.classList.toggle("is-stuck", shouldHaveShadow)
    listTitle.classList.toggle("shadow-md", shouldHaveShadow)
  }
}

// Context for sharing observers across components
class StickyObserverContext(container: Element) {
  val listTitleObserver: IntersectionObserver = new IntersectionObserver(
    (entries, _) => {
      entries.foreach { entry =>
        val sentinel = entry.target.asInstanceOf[HTMLElement]
        val title = sentinel.nextElementSibling.asInstanceOf[HTMLElement]
        val parent = title.parentElement
        val isStuck = !entry.isIntersecting
        stuckState = stuckState.updated(title, isStuck)
        title.classList.toggle("is-stuck", isStuck)
        setShadow(title, isStuck, isList = true)
        updateShadows(parent)
      }
    },
    js.Dynamic
      .literal(root = container, threshold = 0)
      .asInstanceOf[IntersectionObserverInit]
  )

  val subTitleObserver: IntersectionObserver = new IntersectionObserver(
    (entries, _) => {
      entries.foreach { entry =>
        val sentinel = entry.target.asInstanceOf[HTMLElement]
        val subTitle = sentinel.nextElementSibling.asInstanceOf[HTMLElement]
        val isStuck = !entry.isIntersecting
        subTitle.classList.toggle("is-stuck", isStuck)
        setShadow(subTitle, isStuck, isList = false)
        val listItem = subTitle.closest(".list-item")
        if (listItem != null) updateShadows(listItem)
      }
    },
    js.Dynamic
      .literal(
        root = container,
        rootMargin = "-60px 0px 0px 0px",
        threshold = 0
      )
      .asInstanceOf[IntersectionObserverInit]
  )
}

val scrollContainer = div()
val observerContext = new StickyObserverContext(scrollContainer.ref)

// Sentinel elements that register with observers on mount
def listTitleSentinel(): HtmlElement = div(
  cls := "absolute h-px w-full top-0 left-0 pointer-events-none",
  onMountCallback(ctx =>
    observerContext.listTitleObserver.observe(ctx.thisNode.ref)
  ),
  onUnmountCallback(el => observerContext.listTitleObserver.unobserve(el.ref))
)

def subTitleSentinel(): HtmlElement = div(
  cls := "absolute h-px w-full top-0 left-0 pointer-events-none",
  onMountCallback(ctx =>
    observerContext.subTitleObserver.observe(ctx.thisNode.ref)
  ),
  onUnmountCallback(el => observerContext.subTitleObserver.unobserve(el.ref))
)

def renderSubItem(subSection: SubSection): HtmlElement = {
  val collapsed = Var(false)

  div(
    cls := "sub-item relative",
    subTitleSentinel(),
    div(
      cls := "sub-title py-3 px-5 cursor-pointer flex justify-between items-center bg-gray-50 text-gray-700 font-medium select-none sticky top-[60px] hover:bg-gray-200",
      onClick --> { _ => collapsed.update(!_) },
      span(subSection.title),
      span(
        cls <-- collapsed.signal.map(c =>
          if (c) "text-[10px] text-gray-500 -rotate-90 transition-transform"
          else "text-[10px] text-gray-500 transition-transform"
        ),
        "▼"
      )
    ),
    div(
      cls <-- collapsed.signal.map(c =>
        if (c) "py-3 pr-5 pl-9 bg-white text-gray-600 hidden"
        else "py-3 pr-5 pl-9 bg-white text-gray-600"
      ),
      subSection.content.map(text => p(text))
    )
  )
}

def renderSection(section: Section): HtmlElement = {
  val collapsed = Var(false)

  div(
    cls := "list-item bg-white relative",
    listTitleSentinel(),
    div(
      cls := "list-title py-4 px-5 cursor-pointer flex justify-between items-center bg-blue-500 text-white font-semibold text-lg select-none sticky top-0 z-10 hover:bg-blue-600",
      onClick --> { _ => collapsed.update(!_) },
      span(section.title),
      span(
        cls <-- collapsed.signal.map(c =>
          if (c) "text-xs -rotate-90 transition-transform"
          else "text-xs transition-transform"
        ),
        "▼"
      )
    ),
    div(
      cls <-- collapsed.signal.map(c =>
        if (c) "list-content hidden" else "list-content"
      ),
      section.subSections.map(renderSubItem)
    )
  )
}

def app(): HtmlElement = {
  scrollContainer.amend(
    cls := "max-w-3xl mx-auto h-[500px] overflow-y-auto border border-gray-300 bg-white relative",
    idAttr := "scrollContainer",
    sectionData.map(renderSection)
  )
}

@main
def run(): Unit = {
  val container = dom.document.getElementById("app")
  render(container, app())
}
