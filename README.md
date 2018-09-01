# diskmaker-dfs

diskmaker-dfs is a Java program which creates DFS disk images for the BBC Micro emmulator BeebEm.

## Build instructions

With Java 8 and Maven 3 installed run the command `mnv clean package`. Alternately from a docker machine run the script `build.sh`.

The jar file `target/diskmaker-dfs-1.0-SNAPSHOT.jar` will be created.

## Usage instructions

Create the files to transfer to the DFS disk, and then create the configuration file in either txt or XML format, and then run the command:

```
java -jar diskmaker-dfs-1.0-SNAPSHOT.jar <configuration-file>
```

The output location and filename of the disk created are given in the configuration file.  The application supports two formats of configuration file.  XML configuration is recommended.

## XML configuration file example

```
<?xml version="1.0" encoding="UTF-8"?>
<disk>
	<image>poems_xml.ssd</image>
	<label>MyPoemDisk</label>
	<boot-opt>0</boot-opt>
	<files>
		<file>
			<name>Poem1</name>
			<source>Poem1</source>
			<loadAddress>0</loadAddress>
			<execAddress>0</execAddress>
		</file>
		<file>
			<name>Poem2</name>
			<source>Poem2</source>
			<loadAddress>0</loadAddress>
			<execAddress>0</execAddress>
		</file>
	</files>
</disk>
```

## Value in XML configuration files.

All file paths given must be relative to the configuration file.  If a file is in the same folder as the configuration file then no file path is required.

- `image` is the output file to which the disk image will be written.  A file extension `.ssd` is traditional.

- `label` is the volume label written to the disk file.

- `boot-opt` is the auto-boot mode (0 for off, 1 for load, 2 for run, 3 for exec).

Within each file:

- `name` is the name of the file on the disk. It may include a directory name (e.g. `X.MYFILE`).

- `source` is the name of the file on the host computer, to be written to the disk.

- `loadAddress` and `execAddress` are only relevant to compiled machine code, and must be given in hexadecimal.  These values can be seen with the *INFO command on the BBC Micro.

## Text configuration files

```
@image=poems_txt.ssd
@label=MyPoemDisk
@!boot=0
Poem1=Poem1
Poem2=Poem2
```

and a second example with a file requiring load and execution addresses

```
@image=../disks/test.img
@label=TestDiskLbl
@!boot=3
!BOOT=!boot.txt
X.HELLO=out/hello
 load=2000
 exec=2000
README=readme.txt
```

## Values in text configuration files

`@image`, `@label` and `@!boot` are as `image`, `label` and `boot-opt` settings in the XML configuration.

Other lines without leading spaces represent `name`=`source` values pairs.  See above for explanations of the terms.

Other lines with leading spaces represent attributs for the proceeding file.  The only supported attributes are `load` and `exec` which are as `loadAddress` and `execAddress` in the XML configuration.
