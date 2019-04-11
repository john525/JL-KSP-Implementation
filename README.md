# JL-KSP-Implementation
My implementation of the single-source k diverse shortest paths algorithm.

<h2>Single-Source K Diverse Shortest Paths</h2>
<p>The single-source k diverse shortest paths algorithm was originally developed by
  <a href="https://academic.oup.com/bioinformatics/article/28/12/i49/268145">Yu-Keng Shih</a>.
The algorithm takes a source node in a weighted directed graph, and attempts to heuristically approximate the k
shortest paths from the source node to every other node in the network.</p>

<p>
Since the top k shortest paths between 2 nodes are usually very similar, the algorithm also takes a diversity threshold
that specifies the minimum percent of nodes that should differ between two paths with the same endpoints.
</p>

<h2>Contribution</h2>
<p>
This is a significantly faster implementation of Shih's original algorithm (both asymptotically and empirically).
The implementation published here also uses significantly less memory (asymptotically and empirically).
</p>

<p>
By developing a more efficient version of the algorithm, we were able to run it on large-scale protein-protein similarity graphs
that would have been previously unfeasible on the Hunter College CS Department's local cluster. By calculating path lengths between
sequentially similar proteins, we can infer structural similarity between proteins with a degree of accuracy comparable to other
widely-used sequence-based protein fold predictors.
</p>

<h2>Research Results</h2>
<p>
  For a full explanation of our research results, visit <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4934902/">PubMed</a>.
</p>
