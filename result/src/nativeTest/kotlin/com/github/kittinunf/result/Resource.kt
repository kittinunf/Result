package com.github.kittinunf.result

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Resource actual constructor(actual val name: String) {
    actual fun exists(): Boolean = throw UnsupportedOperationException("Not implemented")

    actual fun read(): String {
        return when {
            name.contains("lorem_short") ->
                """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
                Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
                """.trimIndent()

            name.contains("lorem_long") ->
                """
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus mollis eleifend libero, id elementum sem sagittis sit amet.
                Donec lorem nulla, mollis eu diam eget, varius facilisis ipsum. Duis turpis lorem, iaculis quis venenatis nec, placerat eget est.
                Nullam posuere leo sit amet diam laoreet finibus. Suspendisse at tempor nibh, sit amet maximus elit.
                Duis est sem, cursus commodo dapibus ultricies, vestibulum in eros. Suspendisse feugiat vel nibh in viverra. Vivamus posuere sodales sapien.
                Donec ex purus, fringilla in lacinia nec, porttitor vitae ante. Suspendisse elementum iaculis nisl, eget aliquam ligula iaculis in.
                Maecenas vestibulum diam in nisi faucibus, vel fringilla tortor facilisis. Nulla vitae tortor volutpat, facilisis nulla id, convallis nulla.
                Integer at orci eleifend, tincidunt ligula in, sagittis sapien. Aliquam molestie arcu venenatis leo congue, quis elementum felis pellentesque.
                Vivamus augue dui, vestibulum quis justo a, rhoncus vulputate dolor. Pellentesque ullamcorper ligula lorem, eget tempor neque viverra vel.
                Vivamus faucibus id lacus sed aliquet. Curabitur faucibus tristique ante eu dictum. Fusce ipsum ipsum, vulputate ac lorem condimentum, sollicitudin feugiat dolor.
                Aliquam erat volutpat. Ut suscipit fringilla rutrum. Vivamus nec posuere libero, a accumsan lectus.
                Aenean quis elit sollicitudin, congue nulla posuere, imperdiet odio. Aenean sit amet ex imperdiet, ultrices enim et, varius erat.
                Donec et ultricies ante. Aenean sagittis, purus euismod bibendum iaculis, augue tellus dignissim odio, vitae consequat leo arcu non dui.
                Vestibulum commodo nibh ex, at porttitor mi tristique in. Donec bibendum, sapien bibendum aliquam pharetra, lorem magna scelerisque tellus, eget vestibulum eros lorem nec urna.
                Proin elementum libero in viverra dignissim. Morbi commodo eget tellus a congue. Suspendisse potenti. Interdum et malesuada fames ac ante ipsum primis in faucibus.
                Praesent euismod aliquet ligula, et consequat tortor sodales feugiat. Praesent et dolor felis.
                """.trimIndent()

            else -> throw RuntimeException("src/commonTest/resources/$name (No such file or directory)")
        }
    }
}
