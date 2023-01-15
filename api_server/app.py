import sys
import json
from revChatGPT.ChatGPT import Chatbot
from flask import Flask, request, abort, send_file
import re

#chatbot = Chatbot({"session_token": sys.argv[1]})
lang_file = open("./en_US.json", "r")
lang_json = "".join(lang_file.readlines())
parsed = json.loads(lang_json,strict=False)

app = Flask(__name__)

@app.get("/chatgpt")
def get_chatgpt_response():
	sound_name = request.args.get("sound_name")
	sound_key = "subtitles." + sound_name.split("minecraft:")[1]
	block_regex = re.compile('block.*.place')
	if block_regex.match(sound_key) != None:
		sound_key = "subtitles.block.generic.place"
	prompt = f'generate a single sentence response to the prompt "what would a Minecraft villager say if they heard {parsed[sound_key]}"'
	#response = chatbot.ask(prompt)

	#return response["message"]
	return prompt


if __name__ == '__main__':
	app.run(host='0.0.0.0')