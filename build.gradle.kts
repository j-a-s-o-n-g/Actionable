import org.jetbrains.intellij.tasks.BuildSearchableOptionsTask
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.RunIdeTask
import org.jetbrains.intellij.tasks.RunPluginVerifierTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask
import kotlin.text.RegexOption.MULTILINE

buildscript {
	repositories {
		mavenCentral()
	}
	
	dependencies {
		classpath("com.guardsquare:proguard-gradle:7.2.1")
	}
}

plugins {
	id("org.jetbrains.intellij") version "1.5.3"
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	java
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

group = "ir.mmd.intellijDev"
version = "3.4.0"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = JavaVersion.VERSION_1_8.toString()
		freeCompilerArgs = listOf("-Xjvm-default=all")
	}
}

intellij {
	plugins.set(
		"com.intellij.java",
		"JavaScript"
	)
	
	version.set("IU-2022.1")
	// version.set("2022.1")
	// version.set("2021.3.1")
	// version.set("2019.1.4")
	// version.set("IU-2018.1")
	// version.set("2017.1")
}

tasks.withType<RunIdeTask> {
	autoReloadPlugins.set(true)
}

task("patchPluginXmlFeatures") {
	val xmlFiles = listOf(
		"src/main/resources/META-INF/plugin.xml",
		"src/main/resources/META-INF/plugin-java.xml",
		"src/main/resources/META-INF/plugin-javascript.xml",
	)
	
	val features = mutableListOf<String>()
	
	xmlFiles.forEach { xmlPath ->
		val xmlFile = file(xmlPath)
		val text = xmlFile.readText()
		val featuresPattern = """<action[\s][\S\s]*?text="(.*?)"[\S\s]*?\/>"""
		features += Regex(featuresPattern, MULTILINE).findAll(text).map { it.groupValues[1] }
	}
	
	val pluginXmlFile = file(xmlFiles[0])
	val text = pluginXmlFile.readText()
	val ulPattern = """<description>[\S\s]*?<ul>([\S\s]*?)<\/ul>[\S\s]*?<\/description>"""
	val range = Regex(ulPattern, MULTILINE).find(text)!!.groups[1]!!.range
	val featuresStr = "\n${features.joinToString("\n") { "<li>$it</li>" }}".prependIndent("\t\t\t") + "\n\t\t"
	val newText = text.replaceRange(range, featuresStr)
	pluginXmlFile.writeText(newText)
}

tasks.withType<PatchPluginXmlTask> {
	dependsOn("patchPluginXmlFeatures")
	version.set(project.version.toString())
	sinceBuild.set("171")
	untilBuild.set("") // to be always the latest version
}

tasks.withType<BuildSearchableOptionsTask> {
	enabled = false
}

tasks.withType<Test> {
	useJUnitPlatform()
}

task<ProGuardTask>("minify") {
	dependsOn(tasks.buildPlugin)
	outputs.upToDateWhen { false }
	
	val gradleModulesPath = "/mnt/8CD64E25D64E103E/CacheFiles/linux/.gradle/caches/modules-2/files-2.1"
	val javaModulesPath = "/usr/lib/jvm/java-18-openjdk-amd64/jmods"
	val ideaLibPath = "com.jetbrains.intellij.idea/ideaIU/2022.1/d6eb959f8d998b33558cfcfeef623f2f09c31416/ideaIU-2022.1/lib"
	val javaPluginLibPath = "com.jetbrains.intellij.idea/ideaIU/2022.1/d6eb959f8d998b33558cfcfeef623f2f09c31416/ideaIU-2022.1/plugins/java/lib"
	val inFile = tasks.buildPlugin.get().archiveFile.get().asFile
	val outFile = "build/minified/${inFile.name}"
	
	optimizationpasses(5)
	printmapping(outFile.replace(".zip", ".mapping"))
	
	dontnote()
	dontwarn("kotlin.**")
	
	libraryjars("$javaModulesPath/java.base.jmod")
	libraryjars("$javaModulesPath/java.desktop.jmod")
	libraryjars("$javaModulesPath/java.datatransfer.jmod")
	
	libraryjars("$gradleModulesPath/$ideaLibPath/")
	libraryjars("$gradleModulesPath/$javaPluginLibPath/")
	
	injars(inFile)
	outjars(outFile)
	
	keepattributes("RuntimeVisibleAnnotations")
	keep("class kotlin.reflect.**")
	
	generateKeepRules("src/main/resources/META-INF/plugin.xml")
	generateKeepRules("src/main/resources/META-INF/plugin-java.xml")
	generateKeepRules("src/main/resources/META-INF/plugin-javascript.xml")
}

tasks.withType<RunPluginVerifierTask> {
	dependsOn("minify")
	
	ideVersions.set(
		"IC-2017.1"
	)
	
	localPaths.set(
		File("/home/mohammad/IDEA/IU-221.5591.19")
	)
	
	distributionFile.set(
		tasks["minify"].outputs.files.files.first()
	)
}

// Helpers

fun ProGuardTask.generateKeepRules(xmlFile: String) {
	val xmlContent = file(xmlFile).readText()
	val classes = mutableSetOf<String>()
	
	Regex(""""ir\.mmd\.intellijDev\.Actionable\..*?"""").findAll(xmlContent).forEach { match ->
		classes += match.value.trim('"').let {
			"class " + if ("State" in it) "$it { public <fields>; }" else it
		}
	}
	
	classes.forEach(::keep)
}

fun <T> ListProperty<T>.set(vararg values: T) = set(listOf(*values))
