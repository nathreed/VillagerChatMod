import sys
import json
from revChatGPT.ChatGPT import Chatbot
from flask import Flask, request, abort, send_file
import re

chatbot = Chatbot({"session_token": sys.argv[1]})
lang_file = open("./en_US.json", "r")
lang_json = "".join(lang_file.readlines())
parsed = json.loads(lang_json,strict=False)

app = Flask(__name__)

@app.get("/talk_to_villager")
def talk_to_villager():
	message = request.args.get("message")

	prompt = f'generate a single sentence response to the prompt "what would a Minecraft villager say if you asked them "{message}""'

	try:
		response = chatbot.ask(prompt)

		return response["message"]
	except:
		return "My ears aren't the best and I didn't hear what you said..."

@app.get("/chatgpt")
def get_chatgpt_response():
	sound_name = request.args.get("sound_name")
	sound_key = "subtitles." + sound_name.split("minecraft:")[1]
	block_regex = re.compile(r'.*block\..*\.(place|break)')
	entity_regex = re.compile(r'.*entity\.(.*)\.(big_fall|burn|death|drink|eat|explode|extinguish_fire|hurt|small_fall|splash|swim)')
	entity_match = entity_regex.match(sound_key)
	print(sound_key)

	try:
		translated_sound_name = None
		if block_regex.match(sound_key) != None:
			sound_key = "subtitles.block.generic.place"
		elif entity_match != None:
			print("MATCHED ON ENTITY")
			translated_sound_name = entity_match.group(1) + " " + parsed[f'subtitles.entity.generic.{entity_match.group(2)}']

		if translated_sound_name == None:
			translated_sound_name = parsed[sound_key]

		prompt = f'generate a single sentence response to the prompt "what would a Minecraft villager say if they heard {translated_sound_name}"'
		response = chatbot.ask(prompt)

		return response["message"]
	except:
		return "I hear something, but my ears aren't the best and I don't know what it was..." # make the villager say something generic to cover for any kind of error we might get


if __name__ == '__main__':
	app.run(host='0.0.0.0')