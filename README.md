# MaixSense-processingTester

A repository created to experiment with and demonstrate the capabilities of the [MaixSense-drivers-java](https://github.com/pbernalpolo/MaixSense-drivers-java).  

This repository features several usage examples, many of which utilize [Processing](https://processing.org/) to provide interactive visualizations.


## Getting Started

Follow these instructions to set up the project in your machine.

1. Clone the repository

```
git clone https://github.com/pbernalpolo/MaixSense-drivers-java
```

2. Update submodules

```
git update submodules --init --recursive
```

3. Import the project.
	In Eclipse do
	1. File > Import...
	2. In section "Git", select "Projects from Git" and click "Next"
	3. Select "Existing local repository" and click "Next"
	4. Add the project using "Add...", "Browse...", and selecting the main folder of the repo (MaixSense-processingTester), click "Open", and "Add"
	5. Select the project from the list and click "Next"
	6. Choose "Import existing Eclipse projects" 

4. Windows users will need to modify the native library location for "gluegen-rt.jar".
	In Eclipse do
	1. Right click on project, and choose "Properties"
	2. Go into "Java Build Path"
	3. In the "Libraries" tab, open the content below "gluegen-rt.jar"
	4. Select "Native library location" and click "Edit..."
	5. Introduce the path to the library location from the workspace (MaixSense-processingTester/lib/processing-4.3-linux-arm64/processing-4.3/core/library/windows-amd64)
	6. "Apply and Close" will introduce the changes.

After this, one should be able to run the examples.


## License

This project is licensed under the [GNU General Public License Version 2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).  
You are free to use, modify, and distribute this project under the terms of this license.

<!-- 
## Donations
If you'd like to support the project, you can make a donation via [Ko-fi](https://ko-fi.com/pablobernalpolo).  
Every contribution, no matter the size, is greatly appreciated and motivates the continued development of this open-source library.
-->

