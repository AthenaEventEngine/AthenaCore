#---------------------------------------------------------------------------#
#                            L2J_EventEngine                                #
#                      By fissban, u3games and Zephyr                       #
#---------------------------------------------------------------------------#

Follow the next steps to install the engine:

1) In Eclipse, do a right click on the project EventEngine (previously, you need to clone it)
2) L2J_EventEngine -> Export.
3) In the new window, select the option "Java" and then "JAR File".
4) Save it as "L2J_EventEngine.jar".
5) Now, take the new jar and paste it on lib folder in the L2J_Server project.

**Note** if you have the last l2j master, you need to add this 'compile files('dist/libs/L2J_EventEngine.jar')' in your build.gradle in L2J_Server project. Otherwhise, just import the jar as 'External library or Jar'.

6) Apply the patchs (you can find them in this directory) in your L2J_Server and L2J_Datapack projects. It's recommended do it manually. Compile your server and install it as you know.
7) Copy the folder './dist/game' allocated in EventEngine project and paste it in the folder where your server is located.
8) Congratulations, you have installed the EventEngine. Enjoy it!
