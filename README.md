CREATE p = (a:Person {name:'bAndy'})-[:WORKS_AT {where:'here'}]->(neo:Job)<-[:WORKS_AT {where:'here'}]-(m:Person {name: 'bMichael'})
RETURN p


MATCH p = (who:Person)->[:WORKS_AT]
RETURN p

MATCH path = (p:Person)-[r {where: 'here'}]->(j:Job)
RETURN path


CREATE p = (h1:Hi {name:'first'})-[:PARENT {tmp:true}]->(h2:Hi {name:'second'})-[:PARENT]->(h3:Hi {name:'third'})
RETURN p


MATCH path = (h1:Hi)-[r:PARENT]->(h2:Hi)
RETURN path

MATCH path = (h1:Hi)-[r:PARENT {tmp:true}]->(h2:Hi)
RETURN path

MATCH path = (h1:Hi)-[*]->(h2:Hi)
RETURN path

MATCH path = (h1:Hi)-[* {tmp:false}]->(h2:Hi)
RETURN path