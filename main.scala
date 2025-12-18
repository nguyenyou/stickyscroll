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

def updateShadows(listItem: Element): Unit = {
  val listTitle = listItem.querySelector(".list-title")
  val stuckSubTitles = listItem.querySelectorAll(".sub-title.is-stuck")

  if (listTitle != null && stuckState.getOrElse(listTitle, false)) {
    if (stuckSubTitles.length == 0) {
      listTitle.classList.add("is-stuck")
    } else {
      listTitle.classList.remove("is-stuck")
    }
  }
}

def setupStickyObservers(container: Element): Unit = {
  // For .list-title elements (sticky at top: 0)
  container.querySelectorAll(".list-title").foreach { titleEl =>
    val title = titleEl.asInstanceOf[HTMLElement]
    val sentinel = dom.document.createElement("div").asInstanceOf[HTMLElement]
    sentinel.className = "sticky-sentinel"
    val parent = title.parentElement
    parent.insertBefore(sentinel, title)

    val options = js.Dynamic
      .literal(
        root = container,
        threshold = 0
      )
      .asInstanceOf[IntersectionObserverInit]

    val observer = new IntersectionObserver(
      (entries, _) => {
        entries.foreach { entry =>
          val isStuck = !entry.isIntersecting
          stuckState = stuckState.updated(title, isStuck)
          if (isStuck) title.classList.add("is-stuck")
          else title.classList.remove("is-stuck")
          updateShadows(parent)
        }
      },
      options
    )
    observer.observe(sentinel)
  }

  // For .sub-title elements (sticky at top: 52px)
  container.querySelectorAll(".sub-title").foreach { subTitleEl =>
    val subTitle = subTitleEl.asInstanceOf[HTMLElement]
    val sentinel = dom.document.createElement("div").asInstanceOf[HTMLElement]
    sentinel.className = "sticky-sentinel"
    val parent = subTitle.parentElement
    parent.insertBefore(sentinel, subTitle)

    val options = js.Dynamic
      .literal(
        root = container,
        rootMargin = "-52px 0px 0px 0px",
        threshold = 0
      )
      .asInstanceOf[IntersectionObserverInit]

    val observer = new IntersectionObserver(
      (entries, _) => {
        entries.foreach { entry =>
          val isStuck = !entry.isIntersecting
          if (isStuck) subTitle.classList.add("is-stuck")
          else subTitle.classList.remove("is-stuck")
          // Update parent list-item's shadow logic
          val listItem = subTitle.closest(".list-item")
          if (listItem != null) updateShadows(listItem)
        }
      },
      options
    )
    observer.observe(sentinel)
  }
}

def renderSubItem(subSection: SubSection): HtmlElement = {
  val collapsed = Var(false)

  div(
    cls := "sub-item",
    cls <-- collapsed.signal.map(c =>
      if (c) "sub-item collapsed" else "sub-item"
    ),
    div(
      cls := "sub-title",
      onClick --> { _ => collapsed.update(!_) },
      subSection.title
    ),
    div(
      cls := "sub-content",
      subSection.content.map(text => p(text))
    )
  )
}

def renderSection(section: Section): HtmlElement = {
  val collapsed = Var(false)

  div(
    cls := "list-item",
    cls <-- collapsed.signal.map(c =>
      if (c) "list-item collapsed" else "list-item"
    ),
    div(
      cls := "list-title",
      onClick --> { _ => collapsed.update(!_) },
      section.title
    ),
    div(
      cls := "list-content",
      section.subSections.map(renderSubItem)
    )
  )
}

def app(): HtmlElement = {
  div(
    cls := "container",
    idAttr := "scrollContainer",
    sectionData.map(renderSection),
    onMountCallback { ctx =>
      setupStickyObservers(ctx.thisNode.ref)
    }
  )
}

@main
def run(): Unit = {
  val container = dom.document.getElementById("app")
  render(container, app())
}
