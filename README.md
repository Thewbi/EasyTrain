# EasyTrain

EasyTrain is a Java Application that talks to the Uhlenbrock Intellibox via p50x commands to control model trains. The trains are controlled on a layout that can be modelled using EasyTrain's user interface.

The purpose of EasyTrain is to operate a model train layout with as little interaction as possible. Once the layout is modelled appropriatly, trains are put onto the layout and a start menu item is selected which let's EasyTrain operate your layout automatically.

In fully automated mode, trains will start from their initial block, randomly select target blocks to drive to and then loop back to their initial block from where they start their random routing again. The automated mode allows the show-casing of your layout without manual planning of routes.

## Usage

Use the up and down arrow keys to zoom into and out of the layout.

When EasyTrain is opened, it tries to connect to the IntelliBox and outputs a message if the connection failed. Without connection to a Intellibox, only simulated operation is possible.

If a connection to the Intellibox is established, EasyTrain will read the status of all blocks from the Intellibox. Reading the status, EasyTrain learns which blocks are used by trains and draws them in a red color. At this point however EasyTrain does not know yet, which blocks are used by which trains! It needs a locomotive's address to send signals for driving.

In order to teach EasyTrain the addresses, first you have to add your trains and their decoder addresses into the train list manually once. 
Then, in a second step, assign the trains manually to the blocks that they are actually located on. Read the section "Placing a Train On a Block" to learn how to achieve this.

When EasyTrain knows which blocks are used and when it knows the address of the trains on those blocks, it can compute routes and send the appropriate commands to drive the trains from block to block.

The algorithm will select three blocks at random, drive the locomotive over each of these three blocks and finally it will return the locomotive to the original block that the locomotive started from.

There are currently the following constraints:
- EasyTrain only drives trains in forward direction. A train will never drive in reverse direction.
- Starting trains from dead end rails will not work properly (it will work for one cycle only) because the train will enter the dead end in reverse orientation that it started from. Because Easy Train does not ever drive trains in reverse but only heading forward, a train that is headed towards the dead end of a rail will never be able to drive again.
- Because of the train above, only start trains from blocks that are part of loops.

To start the algorithm, select RoutingController > Start from the MenuBar. The trains will start their randomly selected routes.

To stop the random operation, click the Stop button at the bottom of the screen.

### Placing a Train On a Block

- Left-Mouse-Click a block to select it. Blocks are rails that have a number to them. The number is the block's address.
- Select Edit > Place Locomotive from the Menu Bar.
- Select one train from the list. To select a train click into the row of the train but do not click on the trains picture.
- Click one of the direction buttons (North, East, South, West) to determine the direction in which the train is facing on the rail so that Easy Train knows if it has to send forwards or backwards commands to operate the train.
- Click Ok to submit your selection.
- The train is now assigned to the block.
- You can repeate the process to assign several trains to several blocks.
- Per block you can only assign a single train.

### Simulated Operation

During Simulated operation, there is no connection to the Intellibox and no commands are sent. Instead trains traverse the layout in simulation. 

To run a simulation

- Place one or more trains on blocks.
- From the MenuBar, select Routing Controller > Start. The Routing controller will now run. 
- Since the operation of the entire application is paused, you have to unpause the application to see the results.
- To unpause the application, use the "Toggle Pause" button which is located at the bottom of the screen.
- The simulation will now select blocks to travel to, and it will paint the layout according to the simulated trains progress along it's route.

