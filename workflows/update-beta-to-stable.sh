#!/bin/bash


cp ./data_refinement_workflow.beta.t2flow ./data_refinement_workflow.t2flow
sed -i 's/http:\/\/ww2\.bgbm\.org\/biovel\/public\/beta\(.*\)/http:\/\/ww2\.bgbm\.org\/biovel\/public\1/g' data_refinement_workflow.t2flow


