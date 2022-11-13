import ipr_one.SwingJTextFieldDemo
import javax.swing.SwingUtilities

fun main() {
    println("Hello World!")
    //remove annoying warning "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint"
    System.setProperty("org.apache.batik.warn_destination", "false")
    SwingUtilities.invokeLater { SwingJTextFieldDemo() }
}