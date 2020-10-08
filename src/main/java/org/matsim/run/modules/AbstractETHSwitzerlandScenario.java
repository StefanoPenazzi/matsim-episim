/**
 * 
 */
package org.matsim.run.modules;

import static org.matsim.episim.model.Transition.to;

import javax.inject.Singleton;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.model.AgeDependentProgressionModel;
import org.matsim.episim.model.ProgressionModel;
import org.matsim.episim.model.Transition;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author stefanopenazzi
 *
 */
public abstract class AbstractETHSwitzerlandScenario extends AbstractModule {
	
	public static final String[] DEFAULT_ACTIVITIES = {
			"pt", "work", "leisure", "education_kiga", "education_primary", "education_secondary", "education_higher", "shopping", "errands", "business", "home"
	};

	public static void addParams(EpisimConfigGroup episimConfig) {
		episimConfig.getOrAddContainerParams("pt")
				.setContactIntensity(10.0);
		episimConfig.getOrAddContainerParams("tr")
				.setContactIntensity(10.0);
		episimConfig.getOrAddContainerParams("leisure")
				.setContactIntensity(5.0);
		episimConfig.getOrAddContainerParams("work")
				.setContactIntensity(10.0);
		episimConfig.getOrAddContainerParams("other")
				.setContactIntensity(4.0);
		episimConfig.getOrAddContainerParams("outside")
				.setContactIntensity(2.0);
		episimConfig.getOrAddContainerParams("home")
				.setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("education_kiga")
		       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("education_primary")
	       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("education_secondary")
	       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("education_higher")
	       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("shop")
		       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("service")
		       .setContactIntensity(3.0);
		episimConfig.getOrAddContainerParams("quarantine_home")
		.setContactIntensity(1.);
	}

	
	/**
	 * Adds base progression config to the given builder.
	 */
	public static Transition.Builder baseProgressionConfig(Transition.Builder builder) {
		return builder
				// Inkubationszeit: Die Inkubationszeit [ ... ] liegt im Mittel (Median) bei 5–6 Tagen (Spannweite 1 bis 14 Tage)
				.from(EpisimPerson.DiseaseStatus.infectedButNotContagious,
						to(EpisimPerson.DiseaseStatus.contagious, Transition.logNormalWithMedianAndStd(4., 4.)))

// Dauer Infektiosität:: Es wurde geschätzt, dass eine relevante Infektiosität bereits zwei Tage vor Symptombeginn vorhanden ist und die höchste Infektiosität am Tag vor dem Symptombeginn liegt
// Dauer Infektiosität: Abstrichproben vom Rachen enthielten vermehrungsfähige Viren bis zum vierten, aus dem Sputum bis zum achten Tag nach Symptombeginn
				.from(EpisimPerson.DiseaseStatus.contagious,
						to(EpisimPerson.DiseaseStatus.showingSymptoms, Transition.logNormalWithMedianAndStd(2., 2.)),    //80%
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(4., 4.)))            //20%

// Erkankungsbeginn -> Hospitalisierung: Eine Studie aus Deutschland zu 50 Patienten mit eher schwereren Verläufen berichtete für alle Patienten eine mittlere (Median) Dauer von vier Tagen (IQR: 1–8 Tage)
				.from(EpisimPerson.DiseaseStatus.showingSymptoms,
						to(EpisimPerson.DiseaseStatus.seriouslySick, Transition.logNormalWithMedianAndStd(5., 5.)),
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(8., 8.)))

// Hospitalisierung -> ITS: In einer chinesischen Fallserie betrug diese Zeitspanne im Mittel (Median) einen Tag (IQR: 0–3 Tage)
				.from(EpisimPerson.DiseaseStatus.seriouslySick,
						to(EpisimPerson.DiseaseStatus.critical, Transition.logNormalWithMedianAndStd(1., 1.)),
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(14., 14.)))

// Dauer des Krankenhausaufenthalts: „WHO-China Joint Mission on Coronavirus Disease 2019“ wird berichtet, dass milde Fälle im Mittel (Median) einen Krankheitsverlauf von zwei Wochen haben und schwere von 3–6 Wochen
				.from(EpisimPerson.DiseaseStatus.critical,
						to(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical, Transition.logNormalWithMedianAndStd(21., 21.)))

				.from(EpisimPerson.DiseaseStatus.seriouslySickAfterCritical,
						to(EpisimPerson.DiseaseStatus.recovered, Transition.logNormalWithMedianAndStd(7., 7.)))
				;

		// yyyy Quellen für alle Aussagen oben??  kai, aug'20

	}

	@Override
	protected void configure() {

		// Use age dependent progression model
		//bind(ProgressionModel.class).to(AgeDependentProgressionModel.class).in(Singleton.class);
		// WARNING: This does not affect runs with --config file, especially batch runs !!
	}

	/**
	 * Provider method that needs to be overwritten to generate fully configured scenario.
	 * Needs to be annotated with {@link Provides} and {@link Singleton}
	 */
	public abstract Config config();

	/**
	 * Creates a config with the default settings for all ETH scenarios.
	 */
	protected static Config getBaseConfig() {

		Config config = ConfigUtils.createConfig(new EpisimConfigGroup());

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		episimConfig.setFacilitiesHandling(EpisimConfigGroup.FacilitiesHandling.bln);

		addParams(episimConfig);

		return config;
	}


}
