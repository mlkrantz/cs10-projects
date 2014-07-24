README.txt
----------
Contents of this repository, other general information

Platform Used
-------------
Mac OSX 10.9.2 (Mavericks) running on a 2012 MacBook Pro

Developed all programs in Java using the Eclipse IDE

General Information
-------------------
This repository contains two projects that I designed and implemented in
CS 10, Problem Solving via Object-Oriented Programming. Both projects are
written in Java, and both can be run in Eclipse or terminal. Each project
is described in detail below.

Huffman Encoding
----------------
The first project implements Huffman encoding in order to compress and decompress 
files, using a provided BufferedBitReader and BufferedBitWriter. All relevant classes
and files can be found in the huffman subdirectory (./huffman), and the program can
compress any sample text file (though a sample file is not provided here). While my
program accounts for several boundary cases (a text file with a single character,
a text file with no characters, etc.), I did not text my program with any non-text
files. Therefore, selecting a non-text file with the FileChooser may yield unexpected
results!

POS Tagger
----------
The second project is a part of speech (POS) tagger, which labels each word in
a given sentence with its part of speech (noun, verb, adjective, etc.). For example,
the following sentence:

The Fulton County Grand Jury said Friday an investigation of Atlanta's 
recent primary election produced `` no evidence '' that any 
irregularities took place .

Would be tagged as follows:

DET NP N ADJ N VD N DET N P NP 
ADJ N N VD `` DET N '' CNJ DET 
N VD N .

My POS tagger uses the following tags:

Tag	Meaning			Examples
---	-------			--------
ADJ	adjective		new, good, high, special, big, local
ADV	adverb			really, already, still, early, now
CNJ	conjunction		and, or, but, if, while, although
DET	determiner		the, a, some, most, every, no
EX	existential		there, there's
FW	foreign word		dolce, ersatz, esprit, quo, maitre
MOD	modal verb		will, can, would, may, must, should
N	noun			year, home, costs, time, education
NP	proper noun		Alison, Africa, April, Washington
NUM	number			twenty-four, fourth, 1991, 14:24
PRO	pronoun			he, their, her, its, my, I, us
P	preposition		on, of, at, with, by, into, under
TO	the word to		to
UH	interjection		ah, bang, ha, whee, hmpf, oops
V	verb			is, has, get, do, make, see, run
VD	past tense		said, took, told, made, asked
VG	present participle	making, going, playing, working
VN	past participle		given, taken, begun, sung
WH	wh determiner		who, which, when, what, where, how

I used a hidden Markov model (trained on the Brown corpus) and the Viterbi
algorithm to determine appropriate tags, and used cross-validation to test
the model upon completion.

Special Considerations
----------------------
Cross-validation of the POS tagger produced values ranging from approximately
87 to 92% accuracy, and accuracy improves as number of partitions increases
and more lines are processed. Right now, cross-validation is set to 5-fold
and 1,000 lines; however, these values can be changed to view different tests
of accuracy (though keep in mind that larger tests may take longer).

The part-of-speech-tagger is generally good at tagging new sentences after it is 
trained on the Brown corpus. However, it is not perfect. Sentences with multiple
interpretations may be tagged differently than anticipated (consider: “I am going
to work now”), and unknown words or misspellings are not handled as well as
they could be. For example,

$ Hakuna Matata
[DET, N]is tagged as (DET, N) because “Hakuna” and “Matata” are both unknowns, and (DET, N)
is the most common transition for a two-word sentence/phrase. Luckily, these errors 
do not occur very frequently, and the part-of-speech tagger for the most part guesses
parts of speech accurately. This is especially true when you train the model from the
entire corpus before soliciting user input (which I did in my program).

For More Information...
-----------------------
All code in the huffman (./huffman) and pos (./pos) subdirectories is thoroughly
commented and documented. Please see documentation and comments for detailed program
descriptions and any further questions. This is the only README in the repository!

