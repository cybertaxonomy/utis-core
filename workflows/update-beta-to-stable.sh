#!/bin/bash

cp ./data_refinement_workflow.beta.t2flow ./data_refinement_workflow.t2flow
sed -i 's/http:\/\/ww2\.bgbm\.org\/biovel\/public\/beta\(.*\)/http:\/\/ww2\.bgbm\.org\/biovel\/public\1/g' data_refinement_workflow.t2flow
sed -i 's/-SNAPSHOT//g' data_refinement_workflow.t2flow
sed -i 's/\[BETA\]\ Data\ Refinement\ Workflow/Data\ Refinement\ Workflow/g' data_refinement_workflow.t2flow
sed -i 's/\[BETA\]\ Data\ Refinement\ Workflow/Data\ Refinement\ Workflow/g' data_refinement_workflow.t2flow
sed -i 's/This\ is\ a\ beta\ version\ of\ the\ Data\ Refinement\ Workflow\ and\ should\ be\ used\ only\ for\ review\ and\ testing\.//g' data_refinement_workflow.t2flow



