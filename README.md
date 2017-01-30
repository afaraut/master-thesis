# master-thesis : "Semantic enrichment and data filtering in social networks for subject centered collection."

##The code was created for a research-oriented project. The main goal was not to optimize the code but to test several methods in order to find the best to achieve our goal. This is why the code you are going to read is not specially optimized.

The main goal of this master thesis is to "Collect the most information on an event described beforehand as a set of words while being robust (i.e. eliminating noise) in real time." A practical example is to begin with a bag of words set according to the subject *(number 1 on the following picture)*. Retrieve data from social networks *(number 2 on the following picture)* before the date of the event, pre-process the data *(number 3 on the following picture)* in order to clean everything wrong (that is to say, all the mistakes made by the users: the misspellings, the grammatical alterations such as incomplete sentences and word distortions etc.). Then process the data *(number 4 on the following picture)* for a numerical representation of texts, cluster the data *(number 5 on the following picture)* in order to regroup posts whose speak about the same topic in the same cluster. Finally get the X top tokens (words and or #hashtags) from each cluster *(number 6 on the following picture)* in order to re-inject those words in the beforehand bag of word *(number 7 on the following picture)* and relaunch the query (enriched).

Feel free to read my master thesis paper [here.](https://github.com/afaraut/master-thesis/blob/master/attachments/Anthony%20FARAUT%20-%20Master%20thesis.pdf)

![Overall idea of the entire project](https://github.com/afaraut/master-thesis/blob/master/attachments/overallIdea.jpg "Overall idea of the entire project.")

