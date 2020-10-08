/**
 * 
 */
package org.matsim.run.modules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import javax.inject.Singleton;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimUtils;
import org.matsim.episim.TracingConfigGroup;
import org.matsim.episim.EpisimUtils.Extrapolation;
import org.matsim.episim.model.FaceMask;
import org.matsim.episim.model.Transition;
import org.matsim.episim.policy.FixedPolicy;
import org.matsim.episim.policy.Restriction;
import org.matsim.episim.policy.FixedPolicy.ConfigBuilder;
import org.matsim.run.modules.SnzBerlinScenario25pct2020.BasePolicyBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author stefanopenazzi
 *
 */
public class SwitzerlandETHScenario extends AbstractETHSwitzerlandScenario {

	/**
	 * Path pointing to the input folder. Can be configured at runtime with EPISIM_INPUT variable.
	 */
	//public static Path INPUT = EpisimUtils.resolveInputPath("../shared-svn/projects/episim/matsim-files/snz/BerlinV2/episim-input");

	/**
	 * The base policy based on actual restrictions in the past and mobility data
	 */
	private static FixedPolicy.ConfigBuilder basePolicy(EpisimConfigGroup episimConfig, double alpha,
														Map<String, Double> ciCorrections, Extrapolation extrapolation,
														long introductionPeriod, Double maskCompliance) throws IOException {

		//ConfigBuilder restrictions = EpisimUtils.createRestrictionsFromCSV2(episimConfig, csv, alpha, extrapolation);
		ConfigBuilder restrictions = FixedPolicy.config();

		for (Map.Entry<String, Double> e : ciCorrections.entrySet()) {

			String date = e.getKey();
			Double ciCorrection = e.getValue();
			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), AbstractETHSwitzerlandScenario.DEFAULT_ACTIVITIES);
			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "quarantine_home");
			restrictions.restrict(date, Restriction.ofCiCorrection(ciCorrection), "pt");
		}
		
		restrictions.restrict("2020-03-16" , 0.2, "leisure","outside")
		.restrict("2020-03-16" , 0.1, "education_primary", "education_kiga")
		.restrict("2020-03-16" , 0.0, "education_secondary", "education_higher")
		.restrict("2020-03-16" , 0.6, "work")
		.restrict("2020-03-16" , 0.2, "shop","services")
		.restrict("2020-03-16" , 0.5, "other")
		.restrict("2020-04-27" , 0.4 , "shop","services")
		.restrict("2020-04-27" , 1.0 , "outside")
		.restrict("2020-04-27" , 0.8, "work")
		.restrict("2020-05-11" , 1.0, "education_kiga")
		.restrict("2020-05-11" , 1.0, "education_primary")
		.restrict("2020-05-11" , 1.0, "education_secondary")
		.restrict("2020-05-11" , 1.0, "shop","services")
		.restrict("2020-05-11" , 0.6, "leisure")
		.restrict("2020-06-08" , 1.0, DEFAULT_ACTIVITIES );


		LocalDate masksCenterDate = LocalDate.of(2020, 4, 27);
		double clothFraction = maskCompliance * 0.9;
		double surgicalFraction = maskCompliance * 0.1;
		// this is the date when it was officially introduced in Berlin, so for the time being we do not make this configurable.  Might be different
		// in MUC and elsewhere!

		for (int ii = 0; ii <= introductionPeriod; ii++) {
			LocalDate date = masksCenterDate.plusDays(-introductionPeriod / 2 + ii);
			restrictions.restrict(date, Restriction.ofMask(Map.of(FaceMask.CLOTH, clothFraction * ii / introductionPeriod,
					FaceMask.SURGICAL, surgicalFraction * ii / introductionPeriod)), "pt", "shop");
		}

		// mask compliance according to bvg
		restrictions.restrict("2020-06-01", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.8 * 0.9, FaceMask.SURGICAL, 0.8 * 0.1)), "pt", "shop");
		restrictions.restrict("2020-07-01", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.85 * 0.9, FaceMask.SURGICAL, 0.85 * 0.1)), "pt", "shop");
		restrictions.restrict("2020-08-01", Restriction.ofMask(Map.of(FaceMask.CLOTH, 0.9 * 0.9, FaceMask.SURGICAL, 0.9 * 0.1)), "pt", "shop");

		return restrictions;
	}

	@Provides
	@Singleton
	public Config config() {

		Config config = getBaseConfig();

		//config.vehicles().setVehiclesFile(INPUT.resolve("de_2020-vehicles.xml").toString());

		EpisimConfigGroup episimConfig = ConfigUtils.addOrGetModule(config, EpisimConfigGroup.class);

		//episimConfig.setInputEventsFile(INPUT.resolve("be_2020_snz_episim_events_25pt_split.xml.gz").toString());

		//config.plans().setInputFile(INPUT.resolve("be_2020_snz_entirePopulation_emptyPlans_withDistricts_25pt_split.xml.gz").toString());

		episimConfig.setInitialInfections(500);
		//episimConfig.setInitialInfectionDistrict("Berlin");
		episimConfig.setSampleSize(0.1);
		episimConfig.setCalibrationParameter(0.000003);
		episimConfig.setMaxContacts(3);
		String startDate = "2020-02-25";
		episimConfig.setStartDate(startDate);
		episimConfig.setHospitalFactor(1.6);
		episimConfig.setProgressionConfig(baseProgressionConfig(Transition.config()).build());
		episimConfig.setInputEventsFile("/home/stefanopenazzi/projects/covid19/data/covid_events.xml.gz");
		

//		TracingConfigGroup tracingConfig = ConfigUtils.addOrGetModule(config, TracingConfigGroup.class);
//		int offset = (int) (ChronoUnit.DAYS.between(episimConfig.getStartDate(), LocalDate.parse("2020-04-01")) + 1);
//		tracingConfig.setPutTraceablePersonsInQuarantineAfterDay(offset);
//		double tracingProbability = 0.5;
//		tracingConfig.setTracingProbability(tracingProbability);
//		tracingConfig.setTracingPeriod_days(14);
//		tracingConfig.setMinContactDuration_sec(15 * 60.);
//		tracingConfig.setQuarantineHouseholdMembers(true);
//		tracingConfig.setEquipmentRate(1.);
//		tracingConfig.setTracingDelay_days(2);
//		tracingConfig.setTracingCapacity_pers_per_day(Map.of(
//				LocalDate.of(2020, 4, 1), 30
//		));

		BasePolicyBuilder basePolicyBuilder = new BasePolicyBuilder(episimConfig);

		episimConfig.setPolicy(FixedPolicy.class, basePolicyBuilder.build().build());

		//config.controler().setOutputDirectory("./output-berlin-25pct-alpha-" + basePolicyBuilder.getAlpha() + "-extrapolation-" + basePolicyBuilder.getExtrapolation() + "-ciCorrections-" + basePolicyBuilder.getCiCorrections() + "-startDate-" + episimConfig.getStartDate() + "-hospitalFactor-" + episimConfig.getHospitalFactor() + "-calibrParam-" + episimConfig.getCalibrationParameter() + "-tracingProba-" + tracingProbability);

//		config.controler().setOutputDirectory("./output-berlin-25pct-unrestricted-calibr-" + episimConfig.getCalibrationParameter());

		return config;
	}

	public static class BasePolicyBuilder {
		private final EpisimConfigGroup episimConfig;

		/*
		 *  alpha = 1 -> ci=0.323
		 *  alpha = 1.2 -> ci=0.360
		 *  alpha = 1.4 -> ci=0.437
		 */
		//?
		private Map<String, Double> ciCorrections = Map.of("2020-03-07", 0.32);
		private double alpha = 1.;
		private Extrapolation extrapolation = Extrapolation.none;
		//private Path csv = INPUT.resolve("BerlinSnzData_daily_until20200830.csv");
		private long introductionPeriod = 14;
		private double maskCompliance = 0.95;

		public BasePolicyBuilder(EpisimConfigGroup episimConfig) {
			this.episimConfig = episimConfig;
		}

		public void setIntroductionPeriod(long introductionPeriod) {
			this.introductionPeriod = introductionPeriod;
		}

		public void setMaskCompliance(double maskCompliance) {
			this.maskCompliance = maskCompliance;
		}

		public void setCsv(Path csv) {
			//this.csv = csv;
		}

		public double getAlpha() {
			return alpha;
		}

		public void setAlpha(double alpha) {
			this.alpha = alpha;
		}

		public void setCiCorrections(Map<String, Double> ciCorrections) {
			this.ciCorrections = ciCorrections;
		}

		public Map<String, Double> getCiCorrections() {
			return ciCorrections;
		}

		public Extrapolation getExtrapolation() {
			return extrapolation;
		}

		public void setExtrapolation(Extrapolation extrapolation) {
			this.extrapolation = extrapolation;
		}

		public ConfigBuilder build() {
			ConfigBuilder configBuilder = null;
			try {
				configBuilder = basePolicy(episimConfig, alpha, ciCorrections, extrapolation, introductionPeriod,
						maskCompliance);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return configBuilder;
		}
	}
}