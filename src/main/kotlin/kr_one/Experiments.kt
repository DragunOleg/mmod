package kr_one

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

    val rg: RandomGenerator = JDKRandomGenerator()
    val g = WeibullDistribution(rg, 10.0, 3.0, WeibullDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY)

    val n = 100

    val data = mapOf<String, Any> (
        "x" to List(n) { g.sample() }
    )

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
    val controller = plotting.Controller(
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
        window.size = Dimension(850, 400)
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}
