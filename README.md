# MATSim Episim

[![Build Status](https://travis-ci.org/matsim-org/matsim-episim.svg?branch=master)](https://travis-ci.org/matsim-org/matsim-episim)
![license](https://img.shields.io/github/license/matsim-org/matsim-episim.svg)
![JDK](https://img.shields.io/badge/JDK-11+-green.svg)


This repository contains an epidemic simulation based on MATSim, provided by the [Transport Systems Planning and Transport Telematics group](https://www.vsp.tu-berlin.de) of [Technische Universität Berlin](https://www.tu-berlin.de).

Dissemination of some scenarios and results produced with the methods in this repository can be found at https://matsim-vsp.github.io/covid-sim/.

<a rel="TU Berlin" href="https://www.vsp.tu-berlin.de"><img src="https://svn.vsp.tu-berlin.de/repos/public-svn/ueber_uns/logo/TUB_Logo.png" width="15%" height="15%"/></a>

### How to use Episim

In order perform an epidemic simulation you first need an MATSim events file.
To get started you can also use a provided event file from the [OpenBerlin Scenario](https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct-schools/output-berlin-v5.4-1pct-schools/berlin-v5.4-1pct-schools.output_events_for_episim.xml.gz).

It is best to run the scenarios with your IDE from the `org.matsim.run` package.
In `org.matsim.run.modules` you can find example scenarios that you may adapt or use to create your own configuration. 

To run them you can create a standalone jar file with:

    mvn package

and run it with (where `OpenBerlinScenario` is the name of the scenario you want to run):

    java -jar matsim-episim-1.0-SNAPSHOT.jar --modules OpenBerlinScenario
    

### Generating mobility traces events file

If you have a MATSim scenario you can also create your own event file to be used for an epidemic simulation.
All that is needed is the population and the event file of one iteration.

Run `DownSampleScenario` to extract the necessary information: 

    java -jar matsim-episim-1.0-SNAPSHOT.jar scenarioCreation downSample 0.1 \
        --population <Path to plans.xml> --events <Path to events.xml>

This example will write a 10% sample into `output` that can be used in the subsequent steps.

### Licenses

The **MATSim program code** in this repository is distributed under the terms of the [GNU Affero General Public License](https://www.gnu.org/licenses/agpl-3.0.html). The MATSim program code are files that reside in the `src` directory hierarchy and typically end with `*.java`.

The **MATSim input files, output files, analysis data and visualizations** are licensed under a <a rel="license" href="https://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
<a rel="license" href="https://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/80x15.png" /></a><br /> MATSim input files are those that are used as input to run MATSim. They often, but not always, have a header pointing to matsim.org. They typically reside in the `scenarios` directory hierarchy. MATSim output files, analysis data, and visualizations are files generated by MATSim runs, or by postprocessing.  They typically reside in a directory hierarchy starting with `output`.

**Other data files**, in particular in `original-input-data`, have their own individual licenses that need to be individually clarified with the copyright holders.


### More information

For more information about the methodology and preliminary results, see VSP working paper https://dx.doi.org/10.14279/depositonce-9835 .

For more information about MATSim, see here: https://www.matsim.org/.
