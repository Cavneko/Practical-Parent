Iteration 2
-----------

With more UI elements being added, the layouts throughout the app were improved and resized to better display them. 
The Flip Coin activity layout was reworked to look the same on all screen sizes and change size dynamically.


The photos that the user puts for children are scaled down. This is so that the app can display many photos in a list
and not lag for a reasonable amount of entries.


We continued to be careful about our image sizes throughout the app and compressed all of them using TinyPNG (tinypng.com)
to reduce the size of our app.


In addition to the task name, we added an optional field for a user to add a longer description to the task. 
This way the task list remains easy to read, displaying only the task name, and the user can input additional information, 
if desired, which can be seen as a description when tapping on the task.


The queue for flipping a coin or completing a task is based upon how recently a child had their turn. The child who had their
turn least recently will be chosen to go next. This means that who's next to flip a coin or complete a task may change if
a new child is added, to be fair to all children (because the new child has never had a turn). Additionally, if the flipping 
order is overridden, that child will be placed at the back of the queue after flipping.


We added children photos where it made sense. Photos are shown in: 
1. The Children activity which lists children. Photos are shown beside the name of each child.
2. The Children edit activity. Shows the photo of the child being edited.
3. The Task activity which lists the different tasks. Photo of the next child for each task is shown beside the task.
4. The Task dialog fragment which inflates the information for a given task. Shows the photo of the next child for that task.
5. Flip Coin activity. Photo is shown of the next child who's turn it is to flip at the top of the activity.
6. Flip Coin override fragment. Photo of each child is shown in the flip queue.
7. Flip History activity. For each flip history entry, a photo is shown of the child corresponding to the entry.
