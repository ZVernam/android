val version by project.extra(System.getenv("PROJECT_VERSION") ?: "snapshot")

fun calculate(version: String): Int {
    if (version == "snapshot") return 6009006
    val values = version.substring(1).split('.')
    val major = Integer.parseInt(values[0]) // 1..∞
    val minor = Integer.parseInt(values[1]) // 0..99
    val build = Integer.parseInt(values[2]) // 0..999
    return (major * 100 + minor) * 1000 + build
}

val calculateVersionCode by project.extra(::calculate)
