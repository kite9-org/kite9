/**
 * Precompiled [org.kite9.java-conventions.gradle.kts][Org_kite9_java_conventions_gradle] script plugin.
 *
 * @see Org_kite9_java_conventions_gradle
 */
public
class Org_kite9_javaConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Org_kite9_java_conventions_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
