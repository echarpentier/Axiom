CREATE TABLE IF NOT EXISTS users(
	user_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	firstname VARCHAR(50) NOT NULL,
	lastname VARCHAR(50) NOT NULL,
	email VARCHAR(100) NOT NULL,
	office_number VARCHAR(5),
	team_id INT,
	app_id VARCHAR(50) NOT NULL,
	app_passwd VARCHAR(200) NOT NULL,
	app_status ENUM('admin','advanced','simple','restricted') NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (user_id),
	CONSTRAINT UNIQUE (firstname,lastname),
	CONSTRAINT UNIQUE (app_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS populations(
	population_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	population_name VARCHAR(50) NOT NULL,
	created DATETIME NOT NULL,
	user_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (population_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id),
	CONSTRAINT UNIQUE (population_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS families(
    family_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    family_name VARCHAR(50) NOT NULL,
    created DATETIME NOT NULL,
    user_id INT UNSIGNED NOT NULL,
    modified TIMESTAMP,

    PRIMARY KEY (family_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT UNIQUE (family_name)
)
ENGINE =InnoDB
;

CREATE TABLE IF NOT EXISTS plates(
	plate_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	plate_name VARCHAR(100) NOT NULL,
	plate_original_name VARCHAR(100) NOT NULL,
	plate_barcode VARCHAR(30) NOT NULL,
	created DATETIME NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (plate_id),
	CONSTRAINT UNIQUE (plate_barcode),
	CONSTRAINT UNIQUE (plate_name),
	CONSTRAINT UNIQUE (plate_original_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS samples(
	sample_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	sample_name VARCHAR(45) NOT NULL,
	sample_original_name VARCHAR(50) NOT NULL,
	plate_coordX INT NOT NULL,
	plate_coordY INT NOT NULL,
	plate_id INT UNSIGNED NOT NULL,
	sample_path VARCHAR(500) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (sample_id),
	FOREIGN KEY (plate_id) REFERENCES plates(plate_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (sample_original_name,plate_id),
	CONSTRAINT UNIQUE (sample_path)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS samples_in_populations(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	sample_id INT UNSIGNED NOT NULL,
	population_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (id),
	FOREIGN KEY (sample_id) REFERENCES samples(sample_id) ON DELETE CASCADE,
	FOREIGN KEY (population_id) REFERENCES populations(population_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (sample_id,population_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS samples_in_families(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	sample_id INT UNSIGNED NOT NULL,
	family_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (id),
	FOREIGN KEY (sample_id) REFERENCES samples(sample_id) ON DELETE CASCADE,
	FOREIGN KEY (family_id) REFERENCES families(family_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (sample_id,family_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS qc_params(
	qc_param_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	qc_name VARCHAR(50) NOT NULL,
	qc_def VARCHAR(300),
	modified TIMESTAMP,

	PRIMARY KEY (qc_param_id),
	CONSTRAINT UNIQUE (qc_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS qc_values(
	qc_value_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	qc_value VARCHAR(50) NOT NULL,
	qc_param_id INT UNSIGNED NOT NULL,
	sample_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (qc_value_id),
	FOREIGN KEY (qc_param_id) REFERENCES qc_params(qc_param_id) ON DELETE CASCADE,
	FOREIGN KEY (sample_id) REFERENCES samples(sample_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (sample_id,qc_param_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS genotyping_analysis(
	geno_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_name VARCHAR(30) NOT NULL,
	folder_path VARCHAR(500) NOT NULL,
	user_id INT UNSIGNED NOT NULL,
	dishQCLimit DOUBLE NOT NULL,
	callRateLimit DOUBLE NOT NULL,
	annot_id INT UNSIGNED NOT NULL,
	library_id INT UNSIGNED NOT NULL,
	executed DATETIME NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (geno_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id),
	FOREIGN KEY (annot_id) REFERENCES annot_files(annot_id),
	FOREIGN KEY (library_id) REFERENCES library_files(library_id),
	CONSTRAINT UNIQUE (geno_name),
	CONSTRAINT UNIQUE (folder_path)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS genotyping_samples(
	geno_sample_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_id INT UNSIGNED NOT NULL,
	sample_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (geno_sample_id),
	FOREIGN KEY (geno_id) REFERENCES genotyping_analysis(geno_id) ON DELETE CASCADE,
	FOREIGN KEY (sample_id) REFERENCES samples(sample_id) ON DELETE RESTRICT,
	CONSTRAINT UNIQUE (geno_id,sample_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS genotyping_runs(
	geno_run_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_sample_id INT UNSIGNED NOT NULL,
	geno_run ENUM('first','second') NOT NULL,
	chp_path VARCHAR(500) NOT NULL,
	modified TIMESTAMP,
	
	PRIMARY KEY (geno_run_id),
	FOREIGN KEY (geno_sample_id) REFERENCES genotyping_samples(geno_sample_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (geno_sample_id,geno_run),
	CONSTRAINT UNIQUE (chp_path)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS genotyping_qc_params(
	geno_qc_param_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_qc_name VARCHAR(50) NOT NULL,
	geno_qc_def VARCHAR(500),
	modified TIMESTAMP,
	
	PRIMARY KEY (geno_qc_param_id),
	CONSTRAINT UNIQUE (geno_qc_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS genotyping_qc_values(
	geno_qc_value_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_qc_value VARCHAR(50) NOT NULL,
	geno_qc_param_id INT UNSIGNED NOT NULL,
	geno_run_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (geno_qc_value_id),
	FOREIGN KEY (geno_qc_param_id) REFERENCES genotyping_qc_params(geno_qc_param_id) ON DELETE CASCADE,
	FOREIGN KEY (geno_run_id) REFERENCES genotyping_runs(geno_run_id) ON DELETE CASCADE,
	CONSTRAINT UNIQUE (geno_qc_param_id,geno_run_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS studies(
	study_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	study_name VARCHAR(30) NOT NULL,
	study_folder_path VARCHAR(500) NOT NULL,
	user_id INT UNSIGNED NOT NULL,
	study_type ENUM('family','case-control') NOT NULL,
        description LONGTEXT,
	created DATETIME NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (study_id),
	FOREIGN KEY (user_id) REFERENCES users(user_id),
	CONSTRAINT UNIQUE (study_name),
	CONSTRAINT UNIQUE (study_folder_path)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS study_samples(
	study_sample_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	study_id INT UNSIGNED NOT NULL,
	geno_run_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (study_sample_id),
	FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
	FOREIGN KEY (geno_run_id) REFERENCES genotyping_runs(geno_run_id) ON DELETE RESTRICT,
	CONSTRAINT UNIQUE (study_id,geno_run_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS pedigrees(
	pedigree_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	study_id INT UNSIGNED NOT NULL,
	user_id INT UNSIGNED NOT NULL,
	state ENUM('uploaded','samples','individuals','sex','status','final') NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (pedigree_id),
	FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
	FOREIGN KEY (user_id) REFERENCES users(user_id),
	CONSTRAINT UNIQUE (study_id,user_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS pedigree_records(
	pedigree_record_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	pedigree_id INT UNSIGNED NOT NULL,
	family_id VARCHAR(30) NOT NULL,
	individual_id VARCHAR(30) NOT NULL,
	father_id VARCHAR(30) NOT NULL,
	mother_id VARCHAR(30) NOT NULL,
	sex ENUM('male','female','unknown') NOT NULL,
	status ENUM('affected','unaffected','unknown') NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (pedigree_record_id),
	FOREIGN KEY (pedigree_id) REFERENCES pedigrees(pedigree_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (pedigree_id,family_id,individual_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS user_qc_params(
	user_qc_param_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	user_qc_name VARCHAR(50) NOT NULL,
	user_qc_def VARCHAR(300),
	modified TIMESTAMP,

	PRIMARY KEY (user_qc_param_id),
	CONSTRAINT UNIQUE (user_qc_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS user_qc_values(
	user_qc_value_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	user_qc_value VARCHAR(50) NOT NULL,
	user_qc_param_id INT UNSIGNED NOT NULL,
	sample_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (user_qc_value_id),
	FOREIGN KEY (user_qc_param_id) REFERENCES user_qc_params(user_qc_param_id) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (sample_id) REFERENCES samples(sample_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (sample_id,user_qc_param_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS subpedigrees(
	subpedigree_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	subpedigree_name VARCHAR(100) NOT NULL,
	pedigree_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (subpedigree_id),
	FOREIGN KEY (pedigree_id) REFERENCES pedigrees(pedigree_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (subpedigree_name,pedigree_id)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS subpedigree_individuals(
	subped_ind_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	subpedigree_id INT UNSIGNED NOT NULL,
	pedigree_record_id INT UNSIGNED NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (subped_ind_id),
	FOREIGN KEY (subpedigree_id) REFERENCES subpedigrees(subpedigree_id) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (pedigree_record_id) REFERENCES pedigree_records(pedigree_record_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (subpedigree_id,pedigree_record_id)
)
ENGINE =InnoDB
;

CREATE TABLE IF NOT EXISTS library_files(
	library_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	library_name VARCHAR(150) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (library_id),
	CONSTRAINT UNIQUE (library_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS annot_files(
	annot_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	annot_name VARCHAR(150) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (annot_id),
	CONSTRAINT UNIQUE (annot_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS snp_lists(
	snp_list_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	snp_list_name VARCHAR(50) NOT NULL,
	snp_list_path VARCHAR(500) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (snp_list_id),
	CONSTRAINT UNIQUE (snp_list_name)
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS cg_geno_calls(
	cg_geno_calls_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	geno_id INT UNSIGNED NOT NULL,
	snp_list_id INT UNSIGNED NOT NULL,
	calls_path VARCHAR(500) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (cg_geno_calls_id),
	FOREIGN KEY (geno_id) REFERENCES genotyping_analysis(geno_id) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (snp_list_id) REFERENCES snp_lists(snp_list_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (calls_path)	
)
ENGINE = InnoDB
;

CREATE TABLE IF NOT EXISTS cg_study_calls(
	cg_study_calls_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
	study_id INT UNSIGNED NOT NULL,
	snp_list_id INT UNSIGNED NOT NULL,
	calls_path VARCHAR(500) NOT NULL,
	modified TIMESTAMP,

	PRIMARY KEY (cg_study_calls_id),
	FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE ON UPDATE CASCADE,
	FOREIGN KEY (snp_list_id) REFERENCES snp_lists(snp_list_id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT UNIQUE (calls_path)	
)
ENGINE = InnoDB
;


INSERT IGNORE INTO users (firstname,lastname,email,office_number,app_id,app_passwd,app_status) VALUES ('Eric','Charpentier','eric.charpentier@univ-nantes.fr','222a','echarpentier',md5('developer'),'admin');

INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_B_IQR','Interquartile range of control GC probe raw intensities (background intensities) in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_B','Mean of control GC probe raw intensities (background intensities) in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_FLD','Linear Discriminant for signal and background in the AT channel, defined as (median_of_GC_probe_intensities - median_of_AT_probe_intensities)^2 / [0.5 * (Axiom_signal_contrast_AT_B_IQR^2 + Axiom_signal_contrast_AT_S_IQR^2)]');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_SBR','Signal to background ratio in the AT channel, defined as Axiom_signal_contrast_AT_S / Axiom_signal_contrast_AT_B');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_S_IQR','The interquartile range of control AT probe raw intensities (signal intensities) in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_AT_S','Mean of control AT probe raw intensities (signal intensities) in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_A_signal_mean','Mean of control A probe raw intensities in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_C_signal_mean','Mean of control C probe raw intensities in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_B_IQR','The interquartile range of control AT probe raw intensities (background intensities) in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_B','Mean of control AT probe raw intensities (background intensities) in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_FLD','Linear Discriminant for signal and background in the GC channel, defined as (median_of_GC_probe_intensities - median_of_AT_probe_intensities)^2 / [0.5 * (Axiom_signal_contrast_GC_B_IQR^2 + Axiom_signal_contrast_GC_S_IQR^2)]');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_SBR','Signal to background ratio in the GC channel, defined as Axiom_signal_contrast_GC_S / Axiom_signal_contrast_GC_B');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_S_IQR','Interquartile range of control GC probe raw intensities (signal intensities) in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_GC_S','Mean of control GC probe raw intensities (signal intensities) in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_G_signal_mean','Mean of control G probe raw intensities in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_signal_contrast_T_signal_mean','Mean of control T probe raw intensities in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_dishqc_DQC','QC metric that evaluates the overlap between the two homozygous peaks (AT versus GC) in contrast space using normalized intensities of control non-polymorphic probes from both channels. It is defined as the fraction of AT probes not within 2 standard deviations of the GC probes in the contrast space');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_dishqc_log_diff_qc','Another cross channel QC metric, defined as mean(log(AT_SBR))/std(log(AT_SBR)) + mean(log(GC_SBR))/std(log(GC_SBR)), where signal and background are calculated for control non-polymorphic probes after intensity normalization');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_varscore_CV_GC','Median of the coefficient of variation for each control GC probeset in the GC channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('axiom_varscore_CV_AT','Median of the coefficient of variation for each control AT probeset in the AT channel');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('cn-probe-chrXY-ratio_gender_meanX','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('cn-probe-chrXY-ratio_gender_meanY','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('cn-probe-chrXY-ratio_gender_ratio','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('cn-probe-chrXY-ratio_gender','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('reagent_version','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('reagent_discrimination_value','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('saturation_AT','');
INSERT IGNORE INTO qc_params (qc_name,qc_def) VALUES ('saturation_GC','');

INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('computed_gender','Estimated gender');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('call_rate','Percentage of SNPs assigned to clusters ignoring X&Y chromosomes');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('total_call_rate','Percentage of total SNPs assigned to clusters');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('het_rate','Percentage of SNPs called AB ignoring X&Y chromosomes');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('total_het_rate','Percentage of total SNPs called AB');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('hom_rate','Percentage of SNPs called AA or BB ignoring X&Y chromosomes');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('total_hom_rate','Percentage of total SNPs called AA or BB');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cluster_distance_mean','Average distance to the cluster center for the called genotype');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cluster_distance_stdev','Standard deviation of the distance to the cluster center for the called genotype');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_summarization_mean','Average of the allele signal estimates (log2 scale)');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_summarization_stdev','Standard deviation of the allele signal estimates (log2 scale)');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_deviation_mean','Average of the absolute difference between the log2 allele signal estimate and its median across all chips');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_deviation_stdev','Standard deviation of the absolute difference between the log2 allele signal estimate and its median across all chips');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_mad_residuals_mean','Average of the median absolute deviation (MAD) between observed probe intensities and probe intensities fitted by the model');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('allele_mad_residuals_stdev','Standard deviation of the median absolute deviation (MAD) between observed probe intensities and probe intensities fitted by the model');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cn-probe-chrXY-ratio_gender_meanX','Average intensity of chrX CN probes');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cn-probe-chrXY-ratio_gender_meanY','Average intensity of chrY CN probes');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cn-probe-chrXY-ratio_gender_ratio','Ratio of average chrY CN probe intensity to average chrX CN probe intensity');
INSERT IGNORE INTO genotyping_qc_params (geno_qc_name,geno_qc_def) VALUES ('cn-probe-chrXY-ratio_gender','Gender estimate based on ratio of chrY to chrX average CN probe intensities');
