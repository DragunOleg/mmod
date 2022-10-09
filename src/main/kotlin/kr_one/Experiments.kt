package kr_one

import plotting_example.Controller
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.random.JDKRandomGenerator
import org.apache.commons.math3.random.RandomGenerator
import org.jetbrains.letsPlot.geom.geomDensity
import org.jetbrains.letsPlot.geom.geomHistogram
import org.jetbrains.letsPlot.letsPlot
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*
import javax.swing.WindowConstants.EXIT_ON_CLOSE

/**
 * try to build weibull distribution without kt-numpy
 */
fun main() {
    //testRandomAndPseudoRandom()
    //builtWeibull()
    builtReal()
    //builtPseudo()
}

fun testRandomAndPseudoRandom() {
    fun generateTenDoubles(random: java.util.Random): List<Double> {
        val result = mutableListOf<Double>()
        repeat(10) {
            result += random.nextDouble()
        }
        return result
    }

    fun generateTenDoubles(random: kotlin.random.Random): List<Double> {
        val result = mutableListOf<Double>()
        repeat(10) {
            result += random.nextDouble()
        }
        return result
    }


    val realRandom = java.util.Random()
    val realRandom2 = java.util.Random()
    val realRandom3 = java.util.Random()

    val pseudoRandom = kotlin.random.Random(10)
    val pseudoRandom2 = kotlin.random.Random(10)
    val pseudoRandom3 = kotlin.random.Random(10)

    val realList = generateTenDoubles(realRandom)
    val realList2 = generateTenDoubles(realRandom2)
    val realList3 = generateTenDoubles(realRandom3)
    println(
        "realList1 == realList2:${realList == realList2} \n" +
                "realList2 == realList3:${realList2 == realList3}"
    )
    println("realList  = $realList")
    println("realList2 = $realList2")
    println("realList3 = $realList3")

    val pseudoList = generateTenDoubles(pseudoRandom)
    val pseudoList2 = generateTenDoubles(pseudoRandom2)
    val pseudoList3 = generateTenDoubles(pseudoRandom3)
    println(
        "pseudoList1 == pseudoList2:${pseudoList == pseudoList2}\n" +
                "pseudoList2 == pseudoList3:${pseudoList2 == pseudoList3}"
    )
    println("pseudoList  = $pseudoList")
    println("pseudoList2 = $pseudoList2")
    println("pseudoList3 = $pseudoList3")

}

fun builtWeibull() {
    val n = 1000
    val rg: RandomGenerator = JDKRandomGenerator()
    val g = WeibullDistribution(rg, 10.0, 3.0, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY)
    val data = mapOf<String, Any>(
        "x" to List(n) { g.sample() }
    )
    plotData(data)
}

fun builtReal() {
    val n = 100000
    val rg = java.util.Random()
    val data = mapOf<String, List<Double>>(
        "x" to List(n) { rg.nextDouble() }
    )
    println("printing 10 first number to compare on rebuilt")
    println("${data["x"]?.subList(0, 10)}")
    println("min: ${data["x"]?.min()} >0? = ${data["x"]?.min()!! > 0}")
    println("max: ${data["x"]?.max()}")
    plotData(data)
}

fun builtPseudo() {
    val n = 100
    val rg = kotlin.random.Random(10)
    val data = mapOf<String, List<Double>>(
        "x" to List(n) { rg.nextDouble() }
    )
    println("printing 10 first number to compare on rebuilt")
    println("${data["x"]?.subList(0, 10)}")
    println("min: ${data["x"]?.min()} min <0 = ${data["x"]?.min()!! > 0}")
    println("max: ${data["x"]?.max()}")
    plotData(data)
}

fun plotData(data: Map<String, Any>) {
    val plots = mapOf(
        "Density" to letsPlot(data) + geomDensity(
            color = "dark-green",
            fill = "green",
            alpha = .3,
            size = 2.0
        ) { x = "x" },
        "Count" to letsPlot(data) + geomHistogram(
            color = "dark-green",
            fill = "green",
            alpha = .3,
            size = 2.0
        ) { x = "x" },

        )

    val selectedPlotKey = plots.keys.first()
    val controller = Controller(
        plots,
        selectedPlotKey,
        false
    )

    val window = JFrame("Example App")
    window.defaultCloseOperation = EXIT_ON_CLOSE
    window.contentPane.layout = BoxLayout(window.contentPane, BoxLayout.Y_AXIS)

    // Add controls
    val controlsPanel = Box.createHorizontalBox().apply {
        // Plot selector
        val plotButtonGroup = ButtonGroup()
        for (key in plots.keys) {
            plotButtonGroup.add(
                JRadioButton(key, key == selectedPlotKey).apply {
                    addActionListener {
                        controller.plotKey = this.text
                    }
                }
            )
        }

        this.add(Box.createHorizontalBox().apply {
            border = BorderFactory.createTitledBorder("Plot")
            for (elem in plotButtonGroup.elements) {
                add(elem)
            }
        })

        // Preserve aspect ratio selector
        val aspectRadioButtonGroup = ButtonGroup()
        aspectRadioButtonGroup.add(JRadioButton("Original", false).apply {
            addActionListener {
                controller.preserveAspectRadio = true
            }
        })
        aspectRadioButtonGroup.add(JRadioButton("Fit container", true).apply {
            addActionListener {
                controller.preserveAspectRadio = false
            }
        })

        this.add(Box.createHorizontalBox().apply {
            border = BorderFactory.createTitledBorder("Aspect ratio")
            for (elem in aspectRadioButtonGroup.elements) {
                add(elem)
            }
        })
    }
    window.contentPane.add(controlsPanel)

    // Add plot panel
    val plotContainerPanel = JPanel(GridLayout())
    window.contentPane.add(plotContainerPanel)

    controller.plotContainerPanel = plotContainerPanel
    controller.rebuildPlotComponent()

    SwingUtilities.invokeLater {
        window.pack()
        window.size = Dimension(1200, 900)
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}
