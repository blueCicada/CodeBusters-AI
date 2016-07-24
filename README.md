# CodeBusters-AI

This repo contains the AI I submitted for CodinGame's <a href="https://www.codingame.com/contests/codebusters">'CodeBusters'</a> contest.
<br>Bots made by players around the world are pitched against each other in turn-based arena-style matches in this week-long contest and ultimately ranked on performance.
<a href="https://www.codingame.com/blog/en/2016/07/codebusters-contest-report.html0">(more info here)</a>.

Although my bot did not meet my expectations, I learnt a lot from the experience and very much enjoyed the unique challenge this contest offered me.
My previous experiences with competitive AI challenges restricted movement to a given graph, whereas in this contest, movement in any direction within the bounds of the arena was permitted.<br>
The inclusion of other game mechanics which I hadn't seen before, particularly the fog of war (limited view range and stunning range), and the ability to stun enemies and make them drop ghosts, added a layer of complexity which I really liked.

I finished in the Gold League (2nd highest league) with a rank of 681/1984 overall (though I was within the top 300 at my highest point).

Following the conclusion of the contest, CodeBusters later returned as a multiplayer game in the AI section of their website.
I may decide to reuse my bot and continue working on it for that at some point, though I will probably do so in a private repo.

<h2>Notes</h2>
<li>Not writing unit tests was a mistake - I thought it would suffice to run my bot and observe its behaviour, but this proved to be very time consuming,
and once I started implementing more advanced methods, this became very difficult, and was part of the reason I couldn't implement my more advanced features in time.</li>
<li>I got a chance to implement a PQ of ghosts to target with a neat heuristic which estimated the time required to reach a ghost.</li>
<li>Trying to push a bunch of new features with minutes to spare, and then blindly reverting to a previous commit after realising that nothing works is a bad idea.
Always allow for more time than you think you'll need.
</li>
