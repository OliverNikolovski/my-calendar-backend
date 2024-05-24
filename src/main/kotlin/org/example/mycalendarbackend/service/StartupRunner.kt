package org.example.mycalendarbackend.service

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream

@Component
class StartupRunner : CommandLineRunner {

    private var expressJsApp: Process? = null

    override fun run(vararg args: String?) = startExpressJsApp().also {
        Runtime.getRuntime().addShutdownHook(Thread(::stopExpressJsApp))
    }

    private fun startExpressJsApp() {
        val directory = File("./src/main/kotlin/org/example/mycalendarbackend/microservice/rruleservice")
        if (!directory.exists()) {
            return
        }

        val os = System.getProperty("os.name").lowercase()
        val processBuilder =
            if (os.contains("win")) ProcessBuilder("cmd.exe", "/c", "npm start")
            else ProcessBuilder("bash", "-c", "npm start")

        expressJsApp = processBuilder
            .directory(directory)
            //.redirectOutput(File("express-app.log"))
            .redirectErrorStream(true) // Redirects error stream to output stream
            .start()

        expressJsApp?.let { process ->
            println("Started Express.js app with PID: ${process.pid()}")
            // Redirect output to the same console
            redirectStream(process.inputStream, System.out)
            redirectStream(process.errorStream, System.err)
        }
    }

    private fun stopExpressJsApp() = expressJsApp?.run { terminateProcessTree(toHandle()) }

    private fun terminateProcessTree(processHandle: ProcessHandle) {
        // Recursively destroy all descendants
        processHandle.children().forEach { terminateProcessTree(it) }

        // Destroy the process itself
        processHandle.destroyForcibly()
    }

    private fun redirectStream(inputStream: InputStream, outputStream: java.io.PrintStream) {
        Thread {
            inputStream.bufferedReader().lines().forEach { outputStream.println(it) }
        }.start()
    }
}