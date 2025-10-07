# Changelog

## [1.1.1](https://github.com/dnpm-dip/mtb-model/compare/mtb-dto-generators-v1.1.0...mtb-dto-generators-v1.1.1) (2025-10-07)


### Bug Fixes

* Added missing CPS-score in ProteinExpression for IHC-Report ([d282f86](https://github.com/dnpm-dip/mtb-model/commit/d282f868e9d5981b40a8e6c6697919e3d6834568))

## [1.1.0](https://github.com/dnpm-dip/mtb-model/compare/mtb-dto-generators-v1.0.0...mtb-dto-generators-v1.1.0) (2025-08-19)


### Features

* Adaptation to changed cardinality on diagnosis date ([0aae381](https://github.com/dnpm-dip/mtb-model/commit/0aae3812afe598a247466536fb9e966a2ff2c6da))
* Adaptations to refactored base model or MVH; Adapted Completers to refactored DTOs ([7bbb71d](https://github.com/dnpm-dip/mtb-model/commit/7bbb71d321d75db0eacda1f9a9a657eb3aa660a2))
* Adapted model to refactored base model using GeneAlterationReference, as well as generators and v1 mappings ([3ff7c74](https://github.com/dnpm-dip/mtb-model/commit/3ff7c74a033d0b2879143e3db68635a8f3ed0f02))
* Adapted MTBCarePlan to refactored base CarePlan, with concomittant adaptations to completers and generators ([8a37513](https://github.com/dnpm-dip/mtb-model/commit/8a37513a41d257c3d5ef1f2ca62170c4e1b23537))
* Adapted to use Chromosome base trait; Changed SNV.dna- and SNV.proteinChange to Code ([231b188](https://github.com/dnpm-dip/mtb-model/commit/231b188a8467de50927d7d950310b425c675f17f))
* Added FamilyMemberHistory (was previously missing) ([3234082](https://github.com/dnpm-dip/mtb-model/commit/3234082af1faf40d6b2faa67f8afcef95d2a5a3b))
* Added forgotten MSI findings to MTBPatientRecord ([31a614b](https://github.com/dnpm-dip/mtb-model/commit/31a614bc011d4ea33b39ab307fdfeae13f8a5138))
* Cardinality adjustments on SNV and MTBMedicationRecommendation ([094396b](https://github.com/dnpm-dip/mtb-model/commit/094396b4841bff4046b170fc56e025e97a01c747))
* Cardinality corrections to ClaimResponse and IHCReport, with concomittant adaptations to Schema and Generators ([45c3ea5](https://github.com/dnpm-dip/mtb-model/commit/45c3ea5164d4455f7c2aed94b5cba6ba88b7c66d))
* Changes to model according to discussion with MTB representatives; Adaptations to generators ([b0bc2aa](https://github.com/dnpm-dip/mtb-model/commit/b0bc2aaa29cada12746e6bce9b84973c4c270043))


### Bug Fixes

* Adapted generation of Study references to yield valid values ([308a55a](https://github.com/dnpm-dip/mtb-model/commit/308a55a6385e9dfec5004e52967c506e14a920c8))
* Adapted generators to provide correct temporal relations among MDAT objects; Specified recommendation type on OncoProcedure ([37ab190](https://github.com/dnpm-dip/mtb-model/commit/37ab19041ea1f1db1afda410a4bdb5f2cde1e92c))
* Adapted Patient generator to optional address ([ddbdc77](https://github.com/dnpm-dip/mtb-model/commit/ddbdc772979f0b3fdacf7fb719499a9a8ce2fe5c))
* Adapted scalac linting and fixed many reported errors (mostly unused imports) ([bd63a20](https://github.com/dnpm-dip/mtb-model/commit/bd63a20a385b02ed13a714f34e4d6e78f2ae1ce4))
* Added forgotten attribute MTBSystemicTherapy.dosage ([27a7d1f](https://github.com/dnpm-dip/mtb-model/commit/27a7d1f7086983fdda256df5fc191d4b54201a30))
* Added generation of DNA- and RNA-Fusions ([ea435ce](https://github.com/dnpm-dip/mtb-model/commit/ea435ce9be9f43e2cacc4879cf8418124e7f9d43))
* Added JSON schema conformity check of generated MTBPatientRecords ([bc8157c](https://github.com/dnpm-dip/mtb-model/commit/bc8157c1e2f2284e8c4e15d1f5b60445a6085ff8))
* Corrected MTBCarePlan to include other therapy recommendations (aside from medication recommendations) ([83eb084](https://github.com/dnpm-dip/mtb-model/commit/83eb084c981517a7db77897f25c4bfdb537dc1a0))
* Correction to NGSReport.Type subset used in generation ([5a2189a](https://github.com/dnpm-dip/mtb-model/commit/5a2189a0c6321acccf95a4a47f4291b2e0dfb611))
* Fixed cardinality of variants in SomaticNGSReport ([aaf9b75](https://github.com/dnpm-dip/mtb-model/commit/aaf9b75838d8d2fd48ace82388d324580da48c46))
* Fixed DTO generators to always contain reference to HistologyReport in Diagnosis; Adaptations to release workflow ([02e9e69](https://github.com/dnpm-dip/mtb-model/commit/02e9e69de7144834fbb19c8a64ac427c6400b4c2))
* Minor clean-up on GuidelineTherapy generator ([5734961](https://github.com/dnpm-dip/mtb-model/commit/573496105ff59cd4e3d9df82fba9934dce077a80))
* Removed obsolete code from generator tests ([3b2d650](https://github.com/dnpm-dip/mtb-model/commit/3b2d6508fc1b5577d1f331972bdc00f861fbb752))
