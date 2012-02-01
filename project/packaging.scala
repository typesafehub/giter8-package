import sbt._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._

object Packaging {

  val settings: Seq[Setting[_]] = packagerSettings ++ Seq(
    // GENERAL LINUX PACKAGING STUFFS
    maintainer := "Josh Suereth <joshua.suereth@typesafe.com>",
    packageSummary := "giter8 project template scaffolding",
    packageDescription := """This provides access to the giter8 tool to generate
  new projects from template projects on github.""",
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      (packageMapping((bd / "g8") -> "/usr/bin/g8")
       withUser "root" withGroup "root" withPerms "0755")
    },
    linuxPackageMappings <+= (sourceDirectory) map { bd =>
      (packageMapping(
        (bd / "linux" / "g8.1") -> "/usr/share/man/man1/g8.1.gz"
      ) withPerms "0644" gzipped) asDocs()
    },
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd / "copyright") -> "/usr/share/doc/g8/copyright"
      ) withPerms "0644" asDocs()
    },  
    linuxPackageMappings <+= (resourceDirectory in Compile) map { bd =>
      packageMapping(
        (bd / "giter8.properties") -> "/etc/giter8/giter8.properties"
      ) withPerms "0644" withConfig()
    }, 
    linuxPackageMappings <+= (sourceDirectory in Linux) map { bd =>
      packageMapping(
        (bd) -> "/usr/share/doc/g8"
      ) asDocs()
    },
    // DEBIAN SPECIFIC    
    name in Debian := "g8",
    version in Debian <<= version,
    debianPackageDependencies in Debian ++= Seq("sbt", "git"),
    linuxPackageMappings in Debian <+= (sourceDirectory) map { bd =>
      (packageMapping(
        (bd / "debian/changelog") -> "/usr/share/doc/g8/changelog.gz"
      ) withUser "root" withGroup "root" withPerms "0644" gzipped) asDocs()
    },
    
    // RPM SPECIFIC
    name in Rpm := "sbt",
    version in Rpm <<= sbtVersion.identity,
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/n8han/giter8"),
    rpmLicense := Some("BSD"),
    
    
    // WINDOWS SPECIFIC
    name in Windows := "sbt",
    lightOptions ++= Seq("-ext", "WixUIExtension", "-cultures:en-us"),
    wixConfig <<= <Wix/>
  )
}
