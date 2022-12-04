from moviepy.editor import *

'''
pip install moviepy
'''

def changeLayout(inputpath):
    listdir = os.listdir(inputpath) 
    mp4namelist = [name for name in listdir if name.endswith('.mp4')]
    for file in mp4namelist:
        filepath = os.path.join(inputpath, file) 
        video = VideoFileClip(filepath)
        list_filepath = list(filepath)
        list_filepath[-1] = '3'
        if list_filepath in listdir:
            continue
        filepath = ''.join(list_filepath)
        print(filepath)
        audio = video.audio
        audio.write_audiofile(filepath)

if __name__ == '__main__':
    vediopath = '.'
    changeLayout(vediopath)
