---


---

<h1 id="iiif-change-discovery-crawler">IIIF Change Discovery Crawler</h1>
<p>A Java implementation of a crawler for harvesting of IIIF data sources. The crawler implements<br>
the <a href="http://preview.iiif.io/api/discovery/api/discovery/0.1">IIIF Change Discovery API 0.1</a> for discovering IIIF resources (e.g. IIIF Manifests), tracking when resources have been added or changed, and harvest them.</p>
<h2 id="functionality">Functionality</h2>
<p>The software is meant to be used as a library for development of applications that will consume the harvested resources. It provides the following functionality:</p>
<ul>
<li>Fully implements the processing algorithm specified in the  <a href="http://preview.iiif.io/api/discovery/api/discovery/0.1">IIIF Change Discovery API 0.1</a></li>
<li>Manages the harvesting status of the IIIF resources over time, allowing for incremental harvesting.</li>
<li>Allows applications to abstract from all the data synchronization mechanisms with the IIIF sources, and focus on their use of the IIIF resources.</li>
<li>Provides a Java programming interface for the development of applications that use the harvested resources.</li>
<li></li>
</ul>
<h2 id="status-of-this-software">Status of this software</h2>
<p>The software is functional but has only been applied in the few IIIF endpoints implementing the <a href="http://preview.iiif.io/api/discovery/api/discovery/0.1">IIIF Change Discovery API 0.1</a></p>
<p>All the possible data flows that occur in incremental harvesting have not been tested. Handling of service and communication failures is also not extensively tested.</p>
<h3 id="acknowledgements">Acknowledgements</h3>
<p>Many thanks to the members of the  <a href="http://iiif.io/community/groups/discovery/">IIIF Discovery Technical Specification Group</a>.</p>

