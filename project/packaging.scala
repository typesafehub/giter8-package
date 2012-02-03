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
        (bd) -> "/usr/share/doc/g8",
        (bd) -> "/etc/giter8"
      ) withPerms "0755" asDocs()
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
    name in Rpm := "g8",
    version in Rpm <<= version,
    rpmRelease := "1",
    rpmVendor := "typesafe",
    rpmUrl := Some("http://github.com/n8han/giter8"),
    rpmLicense := Some("BSD"),
    rpmRequirements ++= Seq("sbt", "git"),
    
    // WINDOWS SPECIFIC
    name in Windows := "g8",
    lightOptions ++= Seq("-ext", "WixUIExtension", "-cultures:en-us"),
    wixConfig <<= (version, resourceDirectory in Compile, sourceDirectory in Windows) map makeWindowsXml
  )
  
  def makeWindowsXml(version: String, rdir: File, wdir: File) = {
    import com.typesafe.packager.windows.WixHelper._
    val (propids, propxml) = generateComponentsAndDirectoryXml(rdir / "giter8.properties", "prop_")
    val (binids, binxml) = generateComponentsAndDirectoryXml(wdir / "g8.bat", "bat_")
    
<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi' xmlns:util='http://schemas.microsoft.com/wix/UtilExtension'>
  <Product Id='8619b63a-4e7f-44a5-8ca6-2b0a16ec864d' 
            Name='Giter8' 
            Language='1033'
            Version={version}
            Manufacturer='Scala Community' 
            UpgradeCode='9d725776-e9b9-4029-ba1c-7841d62855f2'>
      <Package Description='Giter8 template engine'
                Comments='Packaged by Typesafe, Inc.'
                Manufacturer='Scala Community' 
                InstallScope='perMachine'
                InstallerVersion='200' 
                Compressed='yes' />
      <Media Id='1' Cabinet='g8.cab' EmbedCab='yes' />
      <Directory Id='TARGETDIR' Name='SourceDir'>
         <Directory Id='ProgramFilesFolder' Name='PFiles'>
            <Directory Id='INSTALLDIR' Name='giter8'>
            </Directory>
         </Directory>
      </Directory>
      
      <DirectoryRef Id="INSTALLDIR">
        {propxml}
        {binxml}
        <Component Id="Giter8LauncherPath" Guid="6e047fb2-5b4b-44d0-953d-eb9a73062a63">
          <CreateFolder/>
          <Environment Id="PATH" Name="PATH" Value="[INSTALLDIR]" Permanent="no" Part="last" Action="set" System="yes" />
        </Component>
      </DirectoryRef>
      
      <Feature Id='Complete' Title='Giter8 project templater' Description='An application to generate project templates.' Level='1'>
        <Feature Id='g8' Title='g8 script' Level='1' Absent='disallow'>
          { for(ref <- (propids ++ binids)) yield <ComponentRef Id={ref}/> }
        </Feature>
        <Feature Id='Giter8LauncherPathF' Title='Add g8 to windows system PATH' Description='Adds the g8.bat file to the windows system path.' Level='1'>
          <ComponentRef Id='Giter8LauncherPath'/>
        </Feature>
      </Feature>
      <MajorUpgrade 
         AllowDowngrades="no" 
         Schedule="afterInstallInitialize"
         DowngradeErrorMessage="A later version of [ProductName] is already installed.  Setup will no exit."/>  
      <UIRef Id="WixUI_FeatureTree"/>
      <UIRef Id="WixUI_ErrorProgressText"/>
      <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
      <WixVariable Id="WixUILicenseRtf" Value={wdir.getAbsolutePath + "\\License.rtf"} />
  </Product>
</Wix>
  }
}
