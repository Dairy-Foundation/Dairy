all the code in this package is refurbished from [KookyBotz](https://github.com/KookyBotz/)

their original code can be found in either their [PowerPlay](https://github.com/KookyBotz/PowerPlay) 
or [CenterStage](https://github.com/KookyBotz/CenterStage) repositories, specifically in the 
`common/drive/pathing/geometry/profile` sections of these respective repos

what i've done is just rename things to what i think is cleaner, utilize kotlin features,
and not have it ever store its own state 

not storing its state may be performance inefficient since caching is cool but we'll see

for now it's nice and pure

thank you kooky <3