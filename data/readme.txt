UKPConvArg2 Corpus

Please use the following citation:

@inproceedings{Habernal.Gurevych.2016.EMNLP,
  title     = {What makes a convincing argument? Empirical analysis
               and detecting attributes of convincingness in Web argumentation},
  author    = {Habernal, Ivan and Gurevych, Iryna},
  booktitle = {Proceedings of the 2016 Conference on Empirical Methods
               in Natural Language Processing},
  year      = {2016},
  pages     = {1214--1223},
  publisher = {Association for Computational Linguistics},
  address   = {Austin, Texas},
  url       = {https://aclweb.org/anthology/D16-1129}
}

Data format

The UKPConvArg2 corpus is stored in 32 xml files and is based on the UKPConvArg1 corpus
(see our ACL 2016 paper ). The data are licensed under CC-BY (Creative Commons Attribution
4.0 International License).

The source arguments originate from

    createdebate.com licensed under CC-BY
    convinceme.net licensed under Creative Commons Public Domain License


Data formats

XML

Here is an excerpt from the is-the-school-uniform-a-good-or-bad-idea-_bad.xml file; most of it
is self-explanatory.

<?xml version="1.0"?>
<list>
  <annotatedArgumentPair>
    <id>arg198954_arg236737</id>
    <arg1>
      <author>Dilya</author>
      <voteUpCount>1</voteUpCount>
      <voteDownCount>0</voteDownCount>
      <stance>Bad</stance>
      <text>I truly believe that wearing uniform is bad, because it ...</text>
      <id>arg198954</id>
      <originalHTML>&lt;p&gt;I truly believe that wearing uniform is bad, because it ...</originalHTML>
    </arg1>
    <arg2>
      <author>debategirl10</author>
      <voteUpCount>2</voteUpCount>
      <voteDownCount>0</voteDownCount>
      <stance>Bad</stance>
      <text>I think it is bad to wear school uniform because it ...</text>
      <id>arg236737</id>
      <originalHTML>&lt;p&gt;I think it is bad to wear school uniform because it ...</originalHTML>
    </arg2>
    <debateMetaData>
      <title>Is the school uniform a good or bad idea?</title>
      <description/>
      <url>http://www.createdebate.com/debate/show/Is_the_school_uniform_a_good_or_bad_idea</url>
    </debateMetaData>
    <mTurkAssignments>
      <mTurkAssignmentWithReasonUnits>        <!-- this is taken from the UKPConvArg1 corpus -->
        <turkID>A1XJ9VFOJ4LP2X</turkID>
        <hitID>3BPP3MA3TCW33YP4JQ191PJF7XUELD</hitID>
        <assignmentAcceptTime>2016-02-12 04:08:41.0 UTC</assignmentAcceptTime>
        <assignmentSubmitTime>2016-02-12 04:11:44.0 UTC</assignmentSubmitTime>
        <value>a1</value>
        <reason>a1is well written and intelligent</reason>
        <assignmentId>3KRVW3HTZOXR5J90AZRVE3Y3WOTMSA</assignmentId>
        <turkRank>1089</turkRank>
        <turkCompetence>0.6574247589108307</turkCompetence>
        <workerStance>same</workerStance>
        <reasonUnits>                         <!-- these are the new annotations -->
          <reasonUnit>
            <target>a1</target>
            <reasonUnitText>a1is well written and intelligent</reasonUnitText>
            <id>41912</id>
            <averageCompetenceOfOriginalWorkers>0.6574247589108307</averageCompetenceOfOriginalWorkers>
            <!-- this was shown to the crowd-workers -->
            <textForAnnotation>Argument Xis well written and intelligent</textForAnnotation>
            <assignments>    <!-- we have 5 assignments -->
              <mTurkReasonUnitAssignment>
                <turkID>A29PNROOHHGHDY</turkID>
                <hitID>3YCT0L9OMMLU1HX7WY1T3YRKGFZNSI</hitID>
                <assignmentId>3ATPCQ38J9MNZ4CKC2TFNYR4RSJYA7</assignmentId>
                <assignmentAcceptTime>2016-05-23 19:14:44.0 UTC</assignmentAcceptTime>
                <assignmentSubmitTime>2016-05-23 19:16:04.0 UTC</assignmentSubmitTime>
                <value>o9_1</value>
                <turkCompetence>0.7523819982930656</turkCompetence>
              </mTurkReasonUnitAssignment>
              <mTurkReasonUnitAssignment>
                <turkID>A3EEWSPD9OA2U0</turkID>
                <hitID>3YCT0L9OMMLU1HX7WY1T3YRKGFZNSI</hitID>
                <assignmentId>3NGMS9VZTMUCV4CXISNJCEVFIX7FF5</assignmentId>
                <assignmentAcceptTime>2016-05-23 19:23:25.0 UTC</assignmentAcceptTime>
                <assignmentSubmitTime>2016-05-23 19:25:40.0 UTC</assignmentSubmitTime>
                <value>o9_1</value>
                <turkCompetence>0.5031362986342998</turkCompetence>
              </mTurkReasonUnitAssignment>
              <mTurkReasonUnitAssignment>
                <turkID>AAGKOZ4QSVDZE</turkID>
                <hitID>3YCT0L9OMMLU1HX7WY1T3YRKGFZNSI</hitID>
                <assignmentId>3II4UPYCOKJCO9OA523N0DW8DL7QD4</assignmentId>
                <assignmentAcceptTime>2016-05-23 15:28:42.0 UTC</assignmentAcceptTime>
                <assignmentSubmitTime>2016-05-23 15:29:48.0 UTC</assignmentSubmitTime>
                <value>o6_2</value>
                <turkCompetence>0.004208502893839125</turkCompetence>
              </mTurkReasonUnitAssignment>
              <mTurkReasonUnitAssignment>
                <turkID>AI4J8TH0Y11X5</turkID>
                <hitID>3YCT0L9OMMLU1HX7WY1T3YRKGFZNSI</hitID>
                <assignmentId>3U5NZHP4LSE80DSXRL7ORTLW0IDHPE</assignmentId>
                <assignmentAcceptTime>2016-05-23 21:07:50.0 UTC</assignmentAcceptTime>
                <assignmentSubmitTime>2016-05-23 21:08:53.0 UTC</assignmentSubmitTime>
                <value>o9_4</value>
                <turkCompetence>0.6980055503239371</turkCompetence>
              </mTurkReasonUnitAssignment>
              <mTurkReasonUnitAssignment>
                <turkID>ALNQQ6EXMEII7</turkID>
                <hitID>3YCT0L9OMMLU1HX7WY1T3YRKGFZNSI</hitID>
                <assignmentId>3B837J3LDP8I2ZMSF7NC02SDABCSRW</assignmentId>
                <assignmentAcceptTime>2016-05-23 19:24:15.0 UTC</assignmentAcceptTime>
                <assignmentSubmitTime>2016-05-23 19:25:18.0 UTC</assignmentSubmitTime>
                <value>o9_1</value>
                <turkCompetence>0.5951809237465405</turkCompetence>
              </mTurkReasonUnitAssignment>
            </assignments>
            <estimatedGoldLabel>o9_1</estimatedGoldLabel>    <!-- this is the estimated gold label -->
            <!-- some were ignored, some had duplicit text and thus not annotated,
            some were filtered out in previous pre-processing phases -->
            <ignored>false</ignored>
            <duplicate>false</duplicate>
            <filtered>false</filtered>
          </reasonUnit>
        </reasonUnits>
      </mTurkAssignmentWithReasonUnits>
      ....

CSV

The CSV files are generated from the XML files, here is an excerpt from
is-the-school-uniform-a-good-or-bad-idea-_bad.xml.csv

arg198954_arg236737 o8_1,o9_1   I truly believe that ...    I think it is bad to wear ...
arg203444_arg251309 o8_1,o9_1,o5_1,o6_3,o7_3    The school my mother works at, plus the school district ..  Their gay! Actually this ..
...

    Each line is then a single argument pair, tab-separated
        Pair ID (firstArgumentID_secondArgumentID)
        Comma-delimited set of gold labels as presented in the article in Figure 1
        The more convincing argument
        The less convincing argument
            Line breaks are encoded as <br/>